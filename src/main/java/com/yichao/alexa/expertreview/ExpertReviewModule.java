package com.yichao.alexa.expertreview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.yichao.alexa.expertreview.intent.IntentRequestHandler;
import com.yichao.alexa.expertreview.intent.IntentType;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.http.parser.CnetSearchResultPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ExpertReviewModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpertReviewModule.class);

    @Override
    protected void configure() {
        bind(CnetPageClient.class).toInstance(new CnetPageClient());
        bind(CnetSearchResultPageParser.class).toInstance(new CnetSearchResultPageParser());
        install(new IntentRequestHandlerModule());
    }

    @Provides
    @Singleton
    @Named("intentHandlerMap")
    Map<IntentType, IntentRequestHandler> getIntentHandlerMap() {
        return new HashMap<IntentType, IntentRequestHandler>();
    }

    @Provides
    @Singleton
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    @Named("askResponseTextProperties")
    Properties getAskResponseText() {
        return loadProperties("ask-response-text.properties");
    }

    @Provides
    @Singleton
    @Named("tellResponseTextProperties")
    Properties getTellResponseText() {
        return loadProperties("tell-response-text.properties");
    }

    private Properties loadProperties(final String filename) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        try {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } catch (IOException ex) {
            System.err.println("Could not read properties from file " + filename + " in classpath. " + ex);
        }

        return null;
    }
}
