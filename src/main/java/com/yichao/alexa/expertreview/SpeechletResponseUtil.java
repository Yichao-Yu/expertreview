package com.yichao.alexa.expertreview;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yichao.alexa.expertreview.SessionConstant.SESSION_LAST_RESPONSE;

public class SpeechletResponseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeechletResponseUtil.class);

    public static SpeechletResponse newTellResponse(Session session, final String stringOutput, final boolean isOutputSsml) {
        final OutputSpeech outputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }
        final SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech);
        session.setAttribute(SESSION_LAST_RESPONSE, response);
        return response;
    }

    public static SpeechletResponse newAskResponse(Session session, final String stringOutput, final boolean isOutputSsml,
                                                   final String repromptText, final boolean isRepromptSsml) {
        final OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        final SpeechletResponse response = SpeechletResponse.newAskResponse(outputSpeech, reprompt);
        session.setAttribute(SESSION_LAST_RESPONSE, response);
        return response;
    }
}
