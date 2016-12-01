package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.inject.Injector;
import com.yichao.alexa.model.ReviewSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static com.yichao.alexa.expertreview.SessionConstant.*;

@Singleton
public class OtherReviewIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtherReviewIntentHandler.class);

    private static final int productSearchResultLimit = 5;

    @Inject
    OtherReviewIntentHandler(Injector injector) {
        injector.injectMembers(this);
    }

    @Override
    protected IntentType getIntentType() {
        return IntentType.OTHER_REVIEW;
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) {
        if (session.getAttributes().containsKey(SESSION_PROMPTED_SEARCH_RESULT)) {
            final List<ReviewSearchResult> results =
                    getSessionAttribute(session, SESSION_SEARCH_RESULTS, List.class, ReviewSearchResult.class);
            if (results == null || results.isEmpty()) {
                return newTellResponse("Sorry, I did not find any other review.", false);
            } else {
                final int found = getResultSetSize(results);
                String responseString = "Okay. Here are other reviews related to : "
                        + getSessionAttribute(session, SESSION_PROMPTED_SEARCH_RESULT, String.class);
                for (int i = 1; i < found; i++) {
                    responseString = responseString + "No." + (i - 1) + ": " + results.get(i).getTitle() + ". ";
                }
                responseString += "Which one is the one you wanted?";

                LOGGER.debug("Response string: [{}]", responseString);
                session.setAttribute(SESSION_LAST_RESPONSE, responseString);
                return newAskResponse(responseString, false, "", false);
            }
        } else {
            return newTellResponse("No implemented yet", false);
        }
    }

    private int getResultSetSize(List<?> results) {
        return productSearchResultLimit <= 0 ? results.size()
                : results.size() < productSearchResultLimit ? results.size() : productSearchResultLimit;
    }
}
