package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.model.ProductSeller;
import com.yichao.alexa.model.ReviewDetail;
import com.yichao.alexa.model.ReviewSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yichao.alexa.expertreview.SessionConstant.*;

@Singleton
public class ProductReviewSearchIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReviewSearchIntentHandler.class);

    @Override
    protected IntentType getIntentType() {
        return IntentType.PRODUCT_REVIEW_SEARCH;
    }

    @Override
    public SpeechletResponse handleIntentRequest(final Intent intent, final Session session) throws SpeechletException {
        LOGGER.info("Handling review search");
        final String product = intent.getSlot("Product").getValue();
        LOGGER.debug("product is {}", product);

        if (!session.getAttributes().containsKey(SESSION_SEARCHED_PRODUCT) // no searched product
                || !getSessionAttribute(session, SESSION_SEARCHED_PRODUCT, String.class).equals(product)) { // different searched product
            // new search
            resetSearchAttributes(session);
            session.setAttribute(SESSION_SEARCHED_PRODUCT, product);
        }

        final List<ReviewSearchResult> results;
        if (session.getAttributes().containsKey(SESSION_SEARCH_RESULTS)) {
            LOGGER.debug("Load cached results from session attribute {}", SESSION_SEARCH_RESULTS);
            // search was done on the same product, read from cache
            results = getSessionAttribute(session, SESSION_SEARCH_RESULTS, List.class, ReviewSearchResult.class);
        } else {
            LOGGER.debug("Search product {}", product);
            // search reviews
            final String searchPage = cnetPageClient.getSearchResultPage(product);
            results = cnetPageParser.parseSearchResult(searchPage);
            session.setAttribute(SESSION_SEARCH_RESULTS, results);
        }
        if (results == null || results.isEmpty()) {
            session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_END);
            return newTellResponse("I cannot find any product you just mentioned. Please try again.", false, true);
        }
        final ReviewSearchResult firstHit = results.get(0);
        final String responseString = "Find " + firstHit.getReviewType().getDescription() + ": " + firstHit.getTitle()
                + ". Do you want to listen to the review?";

        session.setAttribute(SESSION_PROMPTED_REVIEW, firstHit);
        session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_FIRST_HIT);
        return newAskResponse(responseString, false, "", false);
    }

    @Override
    public SpeechletResponse handleYesIntentRequest(Intent intent, Session session) throws SpeechletException {
        final String state = getSessionAttribute(session, SESSION_FLOW_STATE, String.class);
        final ReviewSearchResult searchResult;
        final ReviewDetail reviewDetail;
        StringBuilder responseString;
        switch (state) {
            case STATE_REVIEW_SEARCH_FIRST_HIT:
                searchResult = getSessionAttribute(session, SESSION_PROMPTED_REVIEW, ReviewSearchResult.class);

                final String reviewPage = cnetPageClient.getReviewPage(searchResult.getUrl());
                reviewDetail = cnetPageParser.parseReviewDetail(reviewPage);

                final String highlightBreak = "<break strength='medium'/>";
                responseString = new StringBuilder("<speak>");
                responseString.append(reviewDetail.toSummarySsmlString());
                responseString.append("<s>Do you want to continue for the Good,");
                responseString.append(highlightBreak);
                responseString.append("the Bad");
                responseString.append(highlightBreak);
                responseString.append("and the Bottom Line ");
                responseString.append(highlightBreak);
                responseString.append("about ");
                responseString.append(reviewDetail.getProduct());
                responseString.append("?</s>");
                responseString.append("</speak>");

                session.setAttribute(SESSION_LAST_RESPONSE, responseString.toString());
                session.setAttribute(SESSION_REVIEW_DETAIL, reviewDetail);
                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_SUMMARY);
                return newAskResponse(responseString.toString(), true, null, false);

            case STATE_REVIEW_SEARCH_SUMMARY:
                reviewDetail = getSessionAttribute(session, SESSION_REVIEW_DETAIL, ReviewDetail.class);
                if (reviewDetail == null) {
                    LOGGER.error("{} is not set in the session", SESSION_REVIEW_DETAIL);
                    throw new SpeechletException(SESSION_REVIEW_DETAIL + " is not set in the session");
                }

                responseString = new StringBuilder();
                responseString.append(reviewDetail.getReviewSummary().toGoodBadBottomLineString());
                responseString.append("Sounds interesting? ");
                responseString.append("Would you like to read the entire review?");

                session.setAttribute(SESSION_LAST_RESPONSE, responseString.toString());
                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE);
                return newAskResponse(responseString.toString(), false, null, false);

            case STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE:
                searchResult = getSessionAttribute(session, SESSION_PROMPTED_REVIEW, ReviewSearchResult.class);
                if (searchResult == null) {
                    LOGGER.error("{} is not set in the session", SESSION_PROMPTED_REVIEW);
                    throw new SpeechletException(SESSION_PROMPTED_REVIEW + " is not set in the session");
                }
                reviewDetail = getSessionAttribute(session, SESSION_REVIEW_DETAIL, ReviewDetail.class);
                if (reviewDetail == null) {
                    LOGGER.error("{} is not set in the session", SESSION_REVIEW_DETAIL);
                    throw new SpeechletException(SESSION_REVIEW_DETAIL + " is not set in the session");
                }
                responseString = new StringBuilder();
                responseString.append("The review has been sent to your Alexa app. Take a look. ");
                responseString.append(getAmazaonOfferResponse(reviewDetail));

                String productImageUrl = getAmazonProductImage(reviewDetail);
                LOGGER.info("Image url is {}", productImageUrl);

                session.setAttribute(SESSION_LAST_RESPONSE, responseString.toString());
                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_LINK_CARD);
                final String cardContent = reviewDetail.toSummaryString() + "\n" + CnetPageClient.CNET_BASE_URL + searchResult.getUrl();

                return newTellResponseWithCard(responseString.toString(), false,
                        searchResult.getReviewType().getDescription() + ": " + searchResult.getTitle(),
                        cardContent, productImageUrl); // TODO load product image from service

            default:
                return newTellResponse("Not Implemented yet", false, true);
        }
    }

    @Override
    public SpeechletResponse handleNoIntentRequest(Intent intent, Session session) throws SpeechletException {
        final String state = getSessionAttribute(session, SESSION_FLOW_STATE, String.class);
        String responseString;
        switch (state) {
            case STATE_REVIEW_SEARCH_FIRST_HIT:
                List<ReviewSearchResult> results = null;
                if (session.getAttributes().containsKey(SESSION_SEARCH_RESULTS)) {
                    LOGGER.debug("Load cached results from session attribute {}", SESSION_SEARCH_RESULTS);
                    // search was done on the same product, read from cache
                    results = getSessionAttribute(session, SESSION_SEARCH_RESULTS, List.class, ReviewSearchResult.class);
                }
                if (results == null || results.size() == 1) {
                    responseString = "Okay. Anytime. Bye.";
                } else {
                    final StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("Okay. I also found some related products. They are: ");
                    results.forEach(e -> responseBuilder.append(e.getTitle()).append("; "));
                    responseBuilder.append("Which one are you interested in?");
                    responseString = responseBuilder.toString();
                }

                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_END);
                return newTellResponse(responseString, false, true);
            case STATE_REVIEW_SEARCH_SUMMARY:
                responseString = "That's alright. But would you like to look at the entire review?";

                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE);
                return newAskResponse(responseString, false, null, false);
            case STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE:
                final ReviewDetail reviewDetail = getSessionAttribute(session, SESSION_REVIEW_DETAIL, ReviewDetail.class);
                if (reviewDetail == null) {
                    LOGGER.error("{} is not set in the session", SESSION_REVIEW_DETAIL);
                    throw new SpeechletException(SESSION_REVIEW_DETAIL + " is not set in the session");
                }

                responseString = "Ok. You have a good day. ";
                responseString += getAmazaonOfferResponse(reviewDetail);

                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_END);
                return newTellResponse(responseString, false, true);
            default:
                return newTellResponse("Not Implemented yet", false, true);
        }
    }

    private String getAmazonProductImage(final ReviewDetail reviewDetail) {
        final List<ProductSeller> amazonSeller = reviewDetail.getSellers().stream()
                .filter(e -> e.getSeller().contains("Amazon.com")).collect(Collectors.toList());
        if (amazonSeller != null && !amazonSeller.isEmpty()) {
            URL rawURL;
            try {
                rawURL = new URL(amazonSeller.get(0).getLink());

                String query = rawURL.getQuery();
                String[] qParameters = query.split("&");
                String destUrl = null;
                for (String param : qParameters) {
                    if (param.startsWith("destUrl=")) {
                        destUrl = param.split("=")[1];
                        break;
                    }
                }
                String decodedDestUrl = URLDecoder.decode(destUrl, "UTF-8");
                rawURL = new URL(decodedDestUrl);
                query = rawURL.getQuery();
                qParameters = query.split("&");
                destUrl = null;
                for (String param : qParameters) {
                    if (param.startsWith("GR_URL=")) {
                        destUrl = param.split("=")[1];
                        break;
                    }
                }
                decodedDestUrl = URLDecoder.decode(URLDecoder.decode(destUrl, "UTF-8"), "UTF-8");
                return amazonPageClient.getProductImageUrlPage(decodedDestUrl);
            } catch (MalformedURLException ex) {
                LOGGER.warn("URL {} is malformed.", amazonSeller.get(0).getLink(), ex);
                return null;
            } catch (UnsupportedEncodingException ex) {
            }
            return "";
        } else {
            return null;
        }
    }

    private String getAmazaonOfferResponse(final ReviewDetail reviewDetail) {
        final StringBuilder responseString = new StringBuilder();
        final List<ProductSeller> amazonSeller = reviewDetail.getSellers().stream()
                .filter(e -> e.getSeller().contains("Amazon.com")).collect(Collectors.toList());

        if (amazonSeller != null && !amazonSeller.isEmpty()) {
            Optional<ProductSeller> lowAmazonSeller = amazonSeller.stream()
                    .filter(e -> reviewDetail.getLowPrice() == null || e.getPrice().equals(reviewDetail.getLowPrice())).findFirst();
            responseString.append("Just one more thing. ");
            if (lowAmazonSeller.isPresent()) {
                final ProductSeller seller = lowAmazonSeller.get();
                responseString.append(" Right now, ");
                responseString.append(seller.getSeller());
                responseString.append(" offers the lowest price at ");
                responseString.append(seller.getPrice());
            } else {
                final ProductSeller seller = amazonSeller.get(0);
                responseString.append("You can purchase from ");
                responseString.append(seller.getSeller());
                responseString.append(" at ");
                responseString.append(seller.getPrice());
            }
            responseString.append(". Amazon Prime member can purchase through Alexa.");
        }
        return responseString.toString();
    }

    private void resetSearchAttributes(final Session session) {
        LOGGER.debug("Reset attributes in session");
        session.setAttribute(SESSION_FLOW_ENTRY, ENTRY_REVIEW_SEARCH);
        session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_INTIAL);
        session.removeAttribute(SESSION_LAST_RESPONSE);
        session.removeAttribute(SESSION_SEARCHED_PRODUCT);
        session.removeAttribute(SESSION_SEARCH_RESULTS);
        session.removeAttribute(SESSION_PROMPTED_REVIEW);
        session.removeAttribute(SESSION_REVIEW_DETAIL);
    }
}
