package com.yichao.alexa.expertreview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.yichao.alexa.expertreview.intent.BuiltInNoIntentHandler;
import com.yichao.alexa.expertreview.intent.BuiltInRepeatIntentHandler;
import com.yichao.alexa.expertreview.intent.BuiltInYesIntentHandler;
import com.yichao.alexa.expertreview.intent.ProductReviewSearchIntentHandler;

import javax.inject.Singleton;

public class IntentRequestHandlerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BuiltInYesIntentHandler.class).toInstance(new BuiltInYesIntentHandler());
        bind(BuiltInNoIntentHandler.class).toInstance(new BuiltInNoIntentHandler());
        bind(BuiltInRepeatIntentHandler.class).toInstance(new BuiltInRepeatIntentHandler());
        bind(ProductReviewSearchIntentHandler.class).toInstance(new ProductReviewSearchIntentHandler());
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

}
