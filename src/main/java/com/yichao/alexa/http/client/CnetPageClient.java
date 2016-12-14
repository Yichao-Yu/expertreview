package com.yichao.alexa.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URLEncoder;

@Singleton
public class CnetPageClient extends HttpPageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnetPageClient.class);

    public static final String CNET_BASE_URL = "https://www.cnet.com";
    private static final String CNET_SEARCH_URL = "https://www.cnet.com/search/?query=%s";
    //    private static final String CNET_SEARCH_URL = "https://www.cnet.com/search/?query=%s&typeName=content_review";

    public String getSearchResultPage(final String query) {
        if (query == null) {
            LOGGER.warn("No product to search.");
            return null;
        }
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

}
