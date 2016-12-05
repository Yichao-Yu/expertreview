package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.yichao.alexa.model.ReviewSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.List;

import static com.yichao.alexa.expertreview.SessionConstant.*;

@Singleton
public class ProductReviewSearchIntentHandler extends BaseIntentHandler implements IntentRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReviewSearchIntentHandler.class);

    @Override
    protected IntentType getIntentType() {
        return IntentType.PRODUCT_REVIEW_SEARCH;
    }

    @Override
    public SpeechletResponse handleIntentRequest(final Intent intent, final Session session) {
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
            results = cnetSearchResultPageParser.parseSearchResult(searchPage);
            session.setAttribute(SESSION_SEARCH_RESULTS, results);
        }

        final ReviewSearchResult firstHit = results.get(0);
        session.setAttribute(SESSION_PROMPTED_SEARCH_RESULT, firstHit);
        session.removeAttribute(SESSION_LAST_RESPONSE);
        final String responseString = "Find " + firstHit.getReviewType().getDescription() + ": " + firstHit.getTitle()
                + ". Want to learn the review?";

        LOGGER.debug("Response string: [{}]", responseString);
        return newAskResponse(responseString, false, "", false);
    }

    private void resetSearchAttributes(final Session session) {
        LOGGER.debug("Reset Search attributes in session");
        session.removeAttribute(SESSION_LAST_RESPONSE);
        session.removeAttribute(SESSION_SEARCHED_PRODUCT);
        session.removeAttribute(SESSION_SEARCH_RESULTS);
        session.removeAttribute(SESSION_PROMPTED_SEARCH_RESULT);
    }
}
