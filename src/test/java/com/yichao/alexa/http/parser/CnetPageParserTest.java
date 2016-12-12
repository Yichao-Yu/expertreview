package com.yichao.alexa.http.parser;

import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.model.ReviewDetail;
import com.yichao.alexa.model.ReviewSearchResult;
import com.yichao.alexa.model.ReviewSummary;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

public class CnetPageParserTest extends BaseIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CnetPageParserTest.class);

    @Inject
    private CnetPageClient cnetPageClient;

    @Inject
    private CnetPageParser cnetPageParser;

    @Inject
    public CnetPageParserTest() {
    }

    @Test
    public void testSearch() throws IOException {
        final String page = cnetPageClient.getSearchResultPage("bose quietcomfort twenty");
        final List<ReviewSearchResult> resultInfo = cnetPageParser.parseSearchResult(page);
        Assert.assertFalse(resultInfo.isEmpty());
    }

    @Test
    public void testReview() throws Exception {
        final String page = cnetPageClient.getReviewPage("/products/bose-quietcomfort-20/");
        final ReviewDetail detail = cnetPageParser.parseReviewDetail(page);
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

    @Test
    public void url() throws Exception {
        URL url = new URL("https://dw.cbsi.com/redir?assetguid=26d84362-02e1-4693-aa5a-169dd0e1f577&contype=review&destUrl=http%3A%2F%2Ftarget.georiot.com%2FProxy.ashx%3Ftsid%3D15276%26dtb%3D1%26GR_URL%3Dhttps%253A%252F%252Fwww.amazon.com%252FBose-SoundSport-Wireless-Headphones-Aqua%252Fdp%252FB01E3SNNGW%25253FSubscriptionId%25253DAKIAJ3NOW7JKGQLTEY4A%252526tag%25253Dcnet-api-20%252526linkCode%25253Dxm2%252526camp%25253D2025%252526creative%25253D165953%252526creativeASIN%25253DB01E3SNNGW&devicetype=desktop&ltype=mlst&merid=300346&mfgid=275436&pagetype=product_main&pdguid=26d84362-02e1-4693-aa5a-169dd0e1f577&sc=US&siteid=1&sl=en&topicbrcrm=Mobile%3ABluetooth%20Headsets&rsid=cbsicnetglobalsite&ttag=amazon&channelid=5&topicguid=1c2710a3-c387-11e2-8208-0291187b029a&assettitle=bose-soundsport-wireless-aqua&seourl=https%3A%2F%2Fwww.cnet.com%2Fproducts%2Fbose-soundsport-wireless%2Freview%2F%0A&viewguid=57f281b0-bcfc-11e6-b518-6942ab47ef2e");
        String query = url.getQuery();
        String[] qParameters = query.split("&");
        String destUrl = null;
        for (String param : qParameters) {
            if (param.startsWith("destUrl=")) {
                destUrl = param.split("=")[1];
                break;
            }
        }
        String decodedDestUrl = URLDecoder.decode(destUrl, "UTF-8");

        url = new URL(decodedDestUrl);
        query = url.getQuery();
        qParameters = query.split("&");
        destUrl = null;
        for (String param : qParameters) {
            if (param.startsWith("GR_URL=")) {
                destUrl = param.split("=")[1];
                break;
            }
        }
        URLDecoder.decode(URLDecoder.decode(destUrl, "UTF-8"), "UTF-8");
        Assert.assertEquals("", query);
    }
}
