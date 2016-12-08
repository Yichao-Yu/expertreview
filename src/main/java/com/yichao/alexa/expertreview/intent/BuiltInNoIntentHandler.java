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
import java.util.Map;

import static com.yichao.alexa.expertreview.SessionConstant.*;

@Singleton
public class BuiltInNoIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltInNoIntentHandler.class);

    @Inject
    @Named("intentHandlerMap")
    private Map<IntentType, IntentRequestHandler> intentHandlerMap;

    @Override
    protected IntentType getIntentType() {
        return IntentType.BUILTIN_NO;
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) throws SpeechletException {
        final String flow = getSessionAttribute(session, SESSION_FLOW_ENTRY, String.class);
        switch (flow) {
            case ENTRY_REVIEW_SEARCH:
                LOGGER.info("Handling review search no intent");
                return intentHandlerMap.get(IntentType.PRODUCT_REVIEW_SEARCH).handleNoIntentRequest(intent, session);
            case ENTRY_PRODUCT_SEARCH:
                LOGGER.info("Handling search no intent");
                return intentHandlerMap.get(IntentType.PRODUCT_SEARCH).handleNoIntentRequest(intent, session);
            default:
                return newTellResponse("Not Implemented yet", false, true);
        }
    }
}
