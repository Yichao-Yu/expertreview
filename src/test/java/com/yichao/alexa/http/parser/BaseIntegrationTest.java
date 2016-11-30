package com.yichao.alexa.http.parser;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yichao.alexa.http.client.CnetPageClient;
import org.junit.Before;

public abstract class BaseIntegrationTest {

    protected Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(CnetPageClient.class);
            bind(CnetSearchResultPageParser.class);
        }
    });

    @Before
    public void setup() {
        injector.injectMembers(this);
    }
}
