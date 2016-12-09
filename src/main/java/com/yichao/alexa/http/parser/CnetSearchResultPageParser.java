package com.yichao.alexa.http.parser;

import com.yichao.alexa.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
public class CnetSearchResultPageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnetSearchResultPageParser.class);

    private static final int LIMITED_SEARCH_RESULT_SIZE = 4;

    // search page
    private static final String SELECTOR_ITEM_LIST = "section.items > .searchItem.product";
    private static final String SELECTOR_ITEM_INFO = ".itemInfo";
    private static final String SELECTOR_TYPE = ".type";
    private static final String SELECTOR_IMAGE = "noscript img";

    // review page
    private static final String SELECTOR_PRODUCT = "h1.headline .itemreviewed";
    private static final String SELECTOR_PRODUCT_TITLE = "h2.productTitle";
    private static final String SELECTOR_AUTHOR = "div.metaData .author > a";
    private static final String SELECTOR_SUMMARY = "div.scoreCard";
    private static final String SELECTOR_PRICE_SUMMARY = "div.priceRange";
    private static final String SELECTOR_MSRP = "a.msrpUnit span.msrp";
    private static final String SELECTOR_LOW_PRICE = "span";
    private static final String SELECTOR_PRICES = "ul > li.reseller";
    private static final String SELECTOR_RATING = "div.editorsRating span.rating";
    private static final String SELECTOR_QUICK_INFO = "div.quickInfo";
    private static final String SELECTOR_GOOD = ".theGood > span";
    private static final String SELECTOR_BAD = ".theBad > span";
    private static final String SELECTOR_BOTTOMLINE = ".theBottomLine > span";


    public List<ReviewSearchResult> parseSearchResult(final String searchPage) {
        if (searchPage == null) {
            return Collections.EMPTY_LIST;
        }
        final Elements searchResultItems = Jsoup.parse(searchPage).select(SELECTOR_ITEM_LIST);
        final List<ReviewSearchResult> resultInfo = new ArrayList<>();

        if (searchResultItems.isEmpty()) {
            LOGGER.info("no item returned.");
            return Collections.emptyList();
        }
        searchResultItems.forEach(elem -> {
            final Element itemInfo = elem.select(SELECTOR_ITEM_INFO).first();
            if (!itemInfo.select(SELECTOR_TYPE).isEmpty()) { // not have type line
                Element anchor = itemInfo.select("a").first();
                Element img = elem.select(SELECTOR_IMAGE).first();
                if ("Review".equalsIgnoreCase(itemInfo.select(SELECTOR_TYPE).first().text())) {
                    resultInfo.add(new ReviewSearchResult(anchor.attr("href"), anchor.text(), ReviewType.EDITOR_REVIEW, img.attr("src")));
                }
            }
        });

        return resultInfo.size() > LIMITED_SEARCH_RESULT_SIZE ? resultInfo.subList(0, LIMITED_SEARCH_RESULT_SIZE) : resultInfo;
    }

    public ReviewDetail parseReviewDetail(final String reviewPage) {
        final Document doc = Jsoup.parse(reviewPage);

        final String productName = getText(doc.select(SELECTOR_PRODUCT));
        final String title = getText(doc.select(SELECTOR_PRODUCT_TITLE));
        final String author = getText(doc.select(SELECTOR_AUTHOR));
        final Elements priceSummary = doc.select(SELECTOR_PRICE_SUMMARY);
        final String msrp = getText(priceSummary.select(SELECTOR_MSRP));
        final String lowPrice = getContent(priceSummary.select(SELECTOR_LOW_PRICE), "lowPrice");
        final Elements prices = doc.select(SELECTOR_PRICES);
        final List<ProductSeller> sellers = new ArrayList<>(prices.size());
        prices.forEach(e ->
                sellers.add(new ProductSeller(getContent(e.select("span"), "seller"),
                        "$" + getContent(e.select("span"), "price"),
                        getContent(e.select("span"), "availability")))
        );
        final Elements summary = doc.select(SELECTOR_SUMMARY);
        final String rating = getText(summary.select(SELECTOR_RATING));
        final Elements quickInfo = summary.select(SELECTOR_QUICK_INFO);
        final String good = getText(quickInfo.select(SELECTOR_GOOD));
        final String bad = getText(quickInfo.select(SELECTOR_BAD));
        final String bottomLine = getText(quickInfo.select(SELECTOR_BOTTOMLINE));

        final ReviewSummary reviewSummary = new ReviewSummary(rating, good, bad, bottomLine);
        return new ReviewDetail(productName, title, author, msrp, "$" + lowPrice, sellers, reviewSummary);
    }

    private String getContent(Elements e, String itempropValue) {
        Element element = e.stream().filter(el -> el.attr("itemprop").equals(itempropValue))
                .findFirst().orElse(null);
        return element != null ? element.attr("content") : null;
    }

    private String getText(Elements elements) {
        Optional<Element> element = Optional.ofNullable(elements.first());
        return element.isPresent() ? element.get().text() : null;
    }
}
