package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.yichao.alexa.model.ReviewDetail;
import com.yichao.alexa.model.ReviewSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

import static com.yichao.alexa.expertreview.SessionConstant.SESSION_LAST_RESPONSE;
import static com.yichao.alexa.expertreview.SessionConstant.SESSION_PROMPTED_SEARCH_RESULT;

@Singleton
public class BuiltInYesIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltInYesIntentHandler.class);

    @Override
    protected IntentType getIntentType() {
        return IntentType.BUILTIN_YES;
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) {
        if (session.getAttributes().containsKey(SESSION_PROMPTED_SEARCH_RESULT)) {
            LOGGER.info("Handling review detail");
            final ReviewSearchResult searchResult = getSessionAttribute(session, SESSION_PROMPTED_SEARCH_RESULT, ReviewSearchResult.class);

            final String reviewPage = cnetPageClient.getReviewPage(searchResult.getUrl());
            final ReviewDetail reviewDetail = cnetSearchResultPageParser.parseReviewDetail(reviewPage);

            final String responseString = reviewDetail.toResponseString();
            LOGGER.debug("Response string: [{}]", responseString);
            session.setAttribute(SESSION_LAST_RESPONSE, responseString);
            return newTellResponse(responseString, true);
        } else {
            return newTellResponse("No implemented yet", false);
        }
    }
}
