package com.yichao.alexa.http.parser;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yichao.alexa.expertreview.ExpertReviewModule;
import com.yichao.alexa.expertreview.IntentRequestHandlerModule;
import org.junit.Before;

public abstract class BaseIntegrationTest {

    protected Injector injector = Guice.createInjector(new ExpertReviewModule(), new IntentRequestHandlerModule());

    @Before
    public void setup() {
        injector.injectMembers(this);
    }
}
