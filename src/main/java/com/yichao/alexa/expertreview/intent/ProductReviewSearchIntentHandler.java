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
import java.util.List;
import java.util.Optional;

import static com.yichao.alexa.expertreview.SessionConstant.*;

@Singleton
public class ProductReviewSearchIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReviewSearchIntentHandler.class);

    private static final int LIMITED_SEARCH_RESULT_SIZE = 4;

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
            results = cnetSearchResultPageParser.parseSearchResult(searchPage).subList(0, LIMITED_SEARCH_RESULT_SIZE);
            session.setAttribute(SESSION_SEARCH_RESULTS, results);
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
                reviewDetail = cnetSearchResultPageParser.parseReviewDetail(reviewPage);

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
                responseString.append(getSessionAttribute(session, SESSION_SEARCHED_PRODUCT, String.class).toString());
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
                responseString.append("The review has been sent to your Alexa device. Take a look.");
                responseString.append(getAmazaonOfferResponse(reviewDetail));

                session.setAttribute(SESSION_LAST_RESPONSE, responseString.toString());
                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_LINK_CARD);
                final String cardContent = reviewDetail.toSummaryString() + "\n" + CnetPageClient.CNET_BASE_URL + searchResult.getUrl();
                return newTellResponseWithCard(responseString.toString(), false,
                        searchResult.getReviewType().getDescription() + ": " + searchResult.getTitle(),
                        cardContent, null);

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
                    responseString = "<speak><s>Okay.</s><s>Hope I can help.</s><s>Bye.</s></speak>";
                } else {
                    final StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("<speak><s>Okay.</s><s>I also found some related products.</s><s>They are<break time='300ms'/>");
                    results.forEach(e -> responseBuilder.append(e.getTitle()).append("<break time='300ms'/>"));
                    responseBuilder.append("</s><s>Which one are you interested in?</s></speak>");
                    responseString = responseBuilder.toString();
                }

                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_END);
                return newTellResponse(responseString, true, true);
            case STATE_REVIEW_SEARCH_SUMMARY:
                responseString = "That's alright. But would you like to read the entire review?";

                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE);
                return newAskResponse(responseString.toString(), true, null, false);
            case STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE:
                final ReviewDetail reviewDetail = getSessionAttribute(session, SESSION_REVIEW_DETAIL, ReviewDetail.class);
                if (reviewDetail == null) {
                    LOGGER.error("{} is not set in the session", SESSION_REVIEW_DETAIL);
                    throw new SpeechletException(SESSION_REVIEW_DETAIL + " is not set in the session");
                }

                responseString = "Have a good day.";
                responseString += getAmazaonOfferResponse(reviewDetail);

                session.setAttribute(SESSION_FLOW_STATE, STATE_REVIEW_SEARCH_END);
                return newTellResponse(responseString.toString(), false, true);
            default:
                return newTellResponse("Not Implemented yet", false, true);
        }
    }

    private String getAmazaonOfferResponse(final ReviewDetail reviewDetail) {
        final StringBuilder responseString = new StringBuilder();
        final List<ProductSeller> amazonSeller = reviewDetail.getSellerAmazon();
        if (amazonSeller != null && !amazonSeller.isEmpty()) {
            Optional<ProductSeller> lowAmazonSeller = amazonSeller.stream().filter(e -> e.getPrice().equals(reviewDetail.getLowPrice())).findFirst();
            responseString.append("Just one more thing. I just want you to know that ");
            if (lowAmazonSeller.isPresent()) {
                final ProductSeller seller = lowAmazonSeller.get();
                responseString.append(seller.getSeller());
                responseString.append(" offers the lowest price at ");
                responseString.append(seller.getPrice());
            } else {
                final ProductSeller seller = amazonSeller.get(0);
                responseString.append("purchase from ");
                responseString.append(seller.getSeller());
                responseString.append(" at ");
                responseString.append(seller.getPrice());
            }
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
