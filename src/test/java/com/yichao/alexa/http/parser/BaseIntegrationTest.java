package com.yichao.alexa.http.parser;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yichao.alexa.expertreview.ExpertReviewModule;
import com.yichao.alexa.expertreview.IntentRequestHandlerModule;
import org.junit.Before;

public abstract class BaseIntegrationTest {

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new ExpertReviewModule());
        injector.injectMembers(this);
    }
}
