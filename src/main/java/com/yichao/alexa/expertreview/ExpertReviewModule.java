package com.yichao.alexa.expertreview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.http.parser.CnetSearchResultPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpertReviewModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpertReviewModule.class);

    @Override
    protected void configure() {
        bind(CnetPageClient.class);
        bind(CnetSearchResultPageParser.class);
    }

    @Provides
    ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }
}
