package com.yichao.alexa.http.parser;

import com.yichao.alexa.model.ReviewDetail;
import com.yichao.alexa.model.ReviewSearchResult;
import com.yichao.alexa.model.ReviewSummary;
import com.yichao.alexa.model.ReviewType;
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

@Singleton
public class CnetSearchResultPageParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnetSearchResultPageParser.class);

    // search page
    private static final String SELECTOR_ITEM_LIST = "section.items > .searchItem.product";
    private static final String SELECTOR_ITEM_INFO = ".itemInfo";
    private static final String SELECTOR_TYPE = ".type";

    // review page
    private static final String SELECTOR_PRODUCT = "h1.headline .itemreviewed";
    private static final String SELECTOR_PRODUCT_TITLE = "h2.productTitle";
    private static final String SELECTOR_AUTHOR = "div.metaData .author > a";
    private static final String SELECTOR_SUMMARY = "div.scoreCard";
    private static final String SELECTOR_RATING = "div.editorsRating  span.rating";
    private static final String SELECTOR_QUICK_INFO = "div.quickInfo";
    private static final String SELECTOR_GOOD = ".theGood > span";
    private static final String SELECTOR_BAD = ".theBad > span";
    private static final String SELECTOR_BOTTOMLINE = ".theBottomLine > span";


    public List<ReviewSearchResult> parseSearchResult(final String searchPage) {
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
                if ("Review".equalsIgnoreCase(itemInfo.select(SELECTOR_TYPE).first().text())) {
                    resultInfo.add(new ReviewSearchResult(anchor.attr("href"), anchor.text(), ReviewType.EDITOR_REVIEW));
                }
            }
        });

        return resultInfo;
    }

    public ReviewDetail parseReviewDetail(final String reviewPage) {
        final Document doc = Jsoup.parse(reviewPage);

        final String productName = doc.select(SELECTOR_PRODUCT).first().text();
        final String title = doc.select(SELECTOR_PRODUCT_TITLE).first().text();
        final String author = doc.select(SELECTOR_AUTHOR).first().text();
        final Elements summary = doc.select(SELECTOR_SUMMARY);
        final String rating = summary.select(SELECTOR_RATING).first().text();
        final Elements quickInfo = summary.select(SELECTOR_QUICK_INFO);
        final String good = quickInfo.select(SELECTOR_GOOD).first().text();
        final String bad = quickInfo.select(SELECTOR_BAD).first().text();
        final String bottomLine = quickInfo.select(SELECTOR_BOTTOMLINE).first().text();

        final ReviewSummary reviewSummary = new ReviewSummary(rating, good, bad, bottomLine);
        return new ReviewDetail(productName, title, author, reviewSummary);
    }
}
