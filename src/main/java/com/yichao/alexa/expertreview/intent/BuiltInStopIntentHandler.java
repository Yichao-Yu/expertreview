package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
public class BuiltInStopIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltInStopIntentHandler.class);

    @Override
    protected IntentType getIntentType() {
        return IntentType.BUILTIN_STOP;
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) throws SpeechletException {
        return newTellResponse(session, "OK. Bye", false, true);
    }
}
