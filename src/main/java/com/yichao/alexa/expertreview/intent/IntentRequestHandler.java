package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
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
    SpeechletResponse handleIntentRequest(Intent intent, Session session) throws SpeechletException;

    /**
     * handle yes intent in a custom intent handler
     *
     * @param intent
     * @param session
     * @return
     */
    SpeechletResponse handleYesIntentRequest(Intent intent, Session session) throws SpeechletException;

    /**
     * handle no intent in a custom intent handler
     *
     * @param intent
     * @param session
     * @return
     */
    SpeechletResponse handleNoIntentRequest(Intent intent, Session session) throws SpeechletException;

    /**
     * handle help intent in a custom intent handler
     *
     * @param intent
     * @param session
     * @return
     */
    SpeechletResponse handleHelpIntentRequest(Intent intent, Session session) throws SpeechletException;

}
