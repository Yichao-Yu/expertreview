package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

import static com.yichao.alexa.expertreview.SessionConstant.SESSION_LAST_RESPONSE;

@Singleton
public class BuiltInRepeatIntentHandler extends BaseIntentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuiltInRepeatIntentHandler.class);

    @Override
    protected IntentType getIntentType() {
        return IntentType.BUILTIN_REPEAT;
    }

    @Override
    public SpeechletResponse handleIntentRequest(Intent intent, Session session) throws SpeechletException {
        if (session.getAttributes().containsKey(SESSION_LAST_RESPONSE)) {
            return getSessionAttribute(session, SESSION_LAST_RESPONSE, SpeechletResponse.class);
        } else {
            return newTellResponse(session, "No previous search. Please tell me what product you want to know the review.", false);
        }
    }
}
