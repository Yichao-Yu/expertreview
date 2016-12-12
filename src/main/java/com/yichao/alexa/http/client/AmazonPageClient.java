package com.yichao.alexa.http.client;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class AmazonPageClient extends HttpPageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonPageClient.class);

    public String getProductImageUrlPage(final String url) {
        if (url == null) {
            LOGGER.warn("No product to search.");
            return null;
        }
        try {
            final String page = getPageContent(url);
            return Jsoup.parse(page).select("#imgTagWrapperId img").first().attr("src");
        } catch (IOException ex) {
            LOGGER.error("Failed to query product from amazon.", ex);
            return null;
        }
    }
}
