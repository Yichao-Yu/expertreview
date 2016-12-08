package com.yichao.alexa.http.client;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URLEncoder;

@Singleton
public class CnetPageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnetPageClient.class);

    public static final String CNET_BASE_URL = "https://www.cnet.com";
    private static final String CNET_SEARCH_URL = "https://www.cnet.com/search/?query=%s";
    //    private static final String CNET_SEARCH_URL = "https://www.cnet.com/search/?query=%s&typeName=content_review";
    private static final int TIMEOUT = 3000;

    public String getSearchResultPage(final String query) {
        try {
            return getPageContent(String.format(CNET_SEARCH_URL, URLEncoder.encode(query, "UTF-8")));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to query reviews from cnet.", ex);
        }
    }

    public String getReviewPage(final String url) {
        try {
            return getPageContent(CNET_BASE_URL + url);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to get page " + url + " from cnet.", ex);
        }
    }

    private String getPageContent(final String url) throws IOException {
        return Request.Get(url)
                .connectTimeout(TIMEOUT)
                .socketTimeout(TIMEOUT * 2)
                .execute().returnContent().asString();
    }
}
