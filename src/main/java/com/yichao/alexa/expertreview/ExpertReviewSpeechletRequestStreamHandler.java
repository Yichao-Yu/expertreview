package com.yichao.alexa.expertreview;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import com.google.inject.Guice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ExpertReviewSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpertReviewSpeechletRequestStreamHandler.class);

    private static final Set<String> supportedApplicationIds;

    static {
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("{api here}");
    }

    public ExpertReviewSpeechletRequestStreamHandler() {
        super(new ExpertReviewSpeechlet(Guice.createInjector(new ExpertReviewModule())), supportedApplicationIds);

    }

}
