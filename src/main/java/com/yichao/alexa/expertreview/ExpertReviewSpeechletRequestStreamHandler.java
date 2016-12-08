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
        supportedApplicationIds.add("amzn1.ask.skill.a4ca5824-0a4a-4420-9e38-a95c0cd1386c");
    }

    public ExpertReviewSpeechletRequestStreamHandler() {
        super(new ExpertReviewSpeechlet(Guice.createInjector(new ExpertReviewModule())), supportedApplicationIds);
    }

}
