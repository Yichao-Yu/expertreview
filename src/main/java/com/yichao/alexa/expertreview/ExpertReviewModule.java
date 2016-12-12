package com.yichao.alexa.expertreview;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.yichao.alexa.expertreview.intent.IntentRequestHandler;
import com.yichao.alexa.expertreview.intent.IntentType;
import com.yichao.alexa.http.client.AmazonPageClient;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.http.parser.CnetPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

public class ExpertReviewModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpertReviewModule.class);

    @Override
    protected void configure() {
        bind(CnetPageClient.class).toInstance(new CnetPageClient());
        bind(CnetPageParser.class).toInstance(new CnetPageParser());
        bind(AmazonPageClient.class).toInstance(new AmazonPageClient());
        install(new IntentRequestHandlerModule());
    }

    @Provides
    @Singleton
    @Named("intentHandlerMap")
    Map<IntentType, IntentRequestHandler> getIntentHandlerMap() {
        return new HashMap<IntentType, IntentRequestHandler>();
    }
}
