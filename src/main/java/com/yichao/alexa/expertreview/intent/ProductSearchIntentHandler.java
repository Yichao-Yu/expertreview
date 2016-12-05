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
public class ProductSearchIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductSearchIntentHandler.class);

    private static final int productSearchResultLimit = 5;

    @Override
    protected IntentType getIntentType() {
        return IntentType.PRODUCT_SEARCH;
    }

    public SpeechletResponse handleIntentRequest(final Intent intent, final Session session) {
        LOGGER.info("Handling product search");
        final String product = intent.getSlot("Product").getValue();
        LOGGER.debug("product is {}", product);

        // new search
        resetSearchAttributes(session);
        session.setAttribute(SESSION_SEARCHED_PRODUCT, product);

        final List<ReviewSearchResult> results;
        LOGGER.debug("Search product {}", product);
        // search reviews
        final String searchPage = cnetPageClient.getSearchResultPage(product);
        results = cnetSearchResultPageParser.parseSearchResult(searchPage);
        session.setAttribute(SESSION_SEARCH_RESULTS, results);

        final int found = getResultSetSize(results);
        if (found == 0) {
            LOGGER.info("No product {} was found", product);
            return newTellResponse("Sorry. I did not find any product called " + product, false);
        }
        String responseString = "Find " + found + " results: ";
        for (int i = 0; i < found; i++) {
            responseString = responseString + "No." + (i + 1) + ": " + results.get(i).getTitle() + ". ";
        }
        responseString += "Which one is the one you wanted?";

        LOGGER.debug("Response string: [{}]", responseString);
        session.setAttribute(SESSION_LAST_RESPONSE, responseString);
        return newAskResponse(responseString, false, "", false);
    }

    private int getResultSetSize(List<?> results) {
        return productSearchResultLimit <= 0 ? results.size()
                : results.size() < productSearchResultLimit ? results.size() : productSearchResultLimit;
    }

    private void resetSearchAttributes(final Session session) {
        LOGGER.debug("Reset Search attributes in session");
        session.removeAttribute(SESSION_LAST_RESPONSE);
        session.removeAttribute(SESSION_SEARCHED_PRODUCT);
        session.removeAttribute(SESSION_SEARCH_RESULTS);
        session.removeAttribute(SESSION_PROMPTED_SEARCH_RESULT);
    }
}
