package com.yichao.alexa.http.parser;

import com.google.inject.Inject;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.model.ReviewDetail;
import com.yichao.alexa.model.ReviewSearchResult;
import com.yichao.alexa.model.ReviewSummary;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CnetSearchResultPageParserTest extends BaseIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnetSearchResultPageParserTest.class);

    @Inject
    private CnetPageClient cnetPageClient;

    @Inject
    private CnetSearchResultPageParser cnetSearchResultPageParser;

    @Test
    public void testSearch() throws IOException {
        final String page = cnetPageClient.getSearchResultPage("bose quietcomfort twenty");
        final List<ReviewSearchResult> resultInfo = cnetSearchResultPageParser.parseSearchResult(page);
        Assert.assertFalse(resultInfo.isEmpty());
    }

    @Test
    public void testReview() throws Exception {
        final String page = cnetPageClient.getReviewPage("/products/bose-quietcomfort-20/");
        final ReviewDetail detail = cnetSearchResultPageParser.parseReviewDetail(page);
        Assert.assertNotNull(detail);
        Assert.assertEquals("Bose QuietComfort 20", detail.getProduct());
        Assert.assertEquals("Expensive, best noise-canceling in-ear headphone", detail.getProductTitle().trim());
        Assert.assertEquals("David Carnoy", detail.getAuthor().trim());
        final ReviewSummary summary = detail.getReviewSummary();
        Assert.assertEquals("4", summary.getRating());
        Assert.assertTrue(summary.getTheGood().startsWith("The Bose QuietComfort 20 and 20i are compact in-ear headphones"));
        Assert.assertTrue(summary.getTheBad().startsWith("Expensive; they don't sound as good as"));
        Assert.assertTrue(summary.getTheBottomLine().startsWith("Despite some downsides, including a high price"));
    }
}
