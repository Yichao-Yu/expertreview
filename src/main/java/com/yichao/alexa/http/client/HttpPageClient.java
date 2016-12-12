package com.yichao.alexa.http.client;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class HttpPageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPageClient.class);

    private static final int TIMEOUT = 3000;

    protected String getPageContent(final String url) throws IOException {
        return Request.Get(url)
                .connectTimeout(TIMEOUT)
                .socketTimeout(TIMEOUT * 2)
                .execute().returnContent().asString();
    }
}
