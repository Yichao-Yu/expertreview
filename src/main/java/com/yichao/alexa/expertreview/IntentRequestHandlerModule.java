package com.yichao.alexa.expertreview;

import com.google.inject.AbstractModule;
import com.yichao.alexa.expertreview.intent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentRequestHandlerModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntentRequestHandlerModule.class);

    @Override
    protected void configure() {
        bind(BuiltInYesIntentHandler.class);
        bind(BuiltInRepeatIntentHandler.class);
        bind(ProductSearchIntentHandler.class);
        bind(ProductReviewSearchIntentHandler.class);
        bind(OtherReviewIntentHandler.class);
    }
}
