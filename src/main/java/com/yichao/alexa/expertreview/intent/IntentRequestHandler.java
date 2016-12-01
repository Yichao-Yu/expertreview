package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * Created by yichaoyu on 11/30/16.
 */
public interface IntentRequestHandler {

    /**
     *
     * @param intent
     * @param session
     * @return
     */
    SpeechletResponse handleIntentRequest(Intent intent, Session session);

}
