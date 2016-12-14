package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Map;

import static com.yichao.alexa.expertreview.SessionConstant.*;

@Singleton
public class BuiltInHelpIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltInHelpIntentHandler.class);

    @Inject
    @Named("intentHandlerMap")
    private Map<IntentType, IntentRequestHandler> intentHandlerMap;

    @Override
    protected IntentType getIntentType() {
        return IntentType.BUILTIN_HELP;
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) throws SpeechletException {
        final String flow = getSessionAttribute(session, SESSION_FLOW_ENTRY, String.class);
        if (flow == null) {
            return getDefaultHelpMessage(session);
        }
        switch (flow) {
            case ENTRY_REVIEW_SEARCH:
                LOGGER.info("Handling review search help intent");
                return intentHandlerMap.get(IntentType.PRODUCT_REVIEW_SEARCH).handleHelpIntentRequest(intent, session);
            default:
                return getDefaultHelpMessage(session);
        }
    }

    private SpeechletResponse getDefaultHelpMessage(final Session session) {
        resetSessionAttributes(session);
        String response = "Expert Review skill will find review for a product you mention. For example, " +
                "just say 'get a review for iphone 7 plus', it will find the product, and confirm if it is the " +
                "right one you want to hear the review for. Simply follow the prompt and, based on your needs, " +
                "say Yes or No to get more detail about the review, or end it. Say Repeat to repeat what Alexa just said. " +
                "Say Help to get the help on each step. Which product you want to get a review for?";
        return newAskResponse(session, response, false, null, false);
    }

    private void resetSessionAttributes(final Session session) {
        session.removeAttribute(SESSION_FLOW_ENTRY);
        session.removeAttribute(SESSION_FLOW_STATE);
        session.removeAttribute(SESSION_LAST_RESPONSE);
        session.removeAttribute(SESSION_SEARCHED_PRODUCT);
        session.removeAttribute(SESSION_SEARCH_RESULTS);
        session.removeAttribute(SESSION_PROMPTED_REVIEW);
        session.removeAttribute(SESSION_REVIEW_DETAIL);
    }
}
