package com.yichao.alexa.expertreview;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.google.inject.Injector;
import com.yichao.alexa.expertreview.intent.IntentRequestHandler;
import com.yichao.alexa.expertreview.intent.IntentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

public class ExpertReviewSpeechlet implements Speechlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpertReviewSpeechlet.class);

    private Map<IntentType, IntentRequestHandler> intentHandlerMap;

    public ExpertReviewSpeechlet(final Injector injector) {
        injector.injectMembers(this);
    }

    @Inject
    private void setIntentHandlerMap(@Named("intentHandlerMap") final Map<IntentType, IntentRequestHandler> intentHandlerMap) {
        this.intentHandlerMap = intentHandlerMap;
    }

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        LOGGER.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        LOGGER.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return getWelcomeResponse(session);
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        LOGGER.info("onIntent {}, requestId={}, sessionId={}", request.getIntent().getName(), request.getRequestId(), session.getSessionId());

        final Intent intent = request.getIntent();
        final String intentName = intent.getName();

        if (LOGGER.isDebugEnabled()) {
            intentHandlerMap.entrySet().forEach(e -> LOGGER.debug("key, value: {}, {}", e.getKey(), e.getValue()));
        }

        final IntentType intentType = IntentType.fromValue(intentName);
        if (intentHandlerMap.containsKey(intentType)) {
            return intentHandlerMap.get(intentType).handleIntentRequest(intent, session);
        }
        return SpeechletResponseUtil.newTellResponse(session, "I dont understand intent " + intentName, false);
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        LOGGER.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    private SpeechletResponse getWelcomeResponse(Session session) {
        String speechOutput = "Welcome to Expert Review. "
                + "Which product would you like to hear review for?";
        String repromptText =
                "I can find you a product review from an professional review site.";

        return SpeechletResponseUtil.newAskResponse(session, speechOutput, false, repromptText, false);
    }

}
