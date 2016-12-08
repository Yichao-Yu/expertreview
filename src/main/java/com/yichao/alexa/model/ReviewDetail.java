package com.yichao.alexa.model;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewDetail {

    private String product; // h1.headline > .itemreviewed
    private String productTitle; // h2.productTitle
    private String author; // div.metaData > .authors
    private String msrp; // div.priceSummary a.msrpUnit span.msrp
    private String lowPrice; // div.priceSummary a.msrpUnit span(lowPrice)
    private List<ProductSeller> sellers; // div.metaData > .authors

    private ReviewSummary reviewSummary; // div.scoreCard

    public ReviewDetail() {
    }

    public ReviewDetail(String product, String productTitle, String author, String msrp, String lowPrice,
                        List<ProductSeller> sellers, ReviewSummary reviewSummary) {
        this.product = product;
        this.productTitle = productTitle;
        this.author = author;
        this.msrp = msrp;
        this.lowPrice = lowPrice;
        this.sellers = sellers;
        this.reviewSummary = reviewSummary;
    }

    public String toSummarySsmlString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<s>");
        sb.append(product);
        sb.append("</s>");
        sb.append("<s>");
        sb.append("Reviewed by ");
        sb.append(author);
        sb.append("</s>");
        sb.append("<s>");
        sb.append(productTitle);
        sb.append("</s>");
        sb.append("<s>");
        sb.append("MSRP is ");
        sb.append(msrp);
        sb.append("</s>");
        sb.append(reviewSummary.toRateSsmlString());
        return sb.toString();
    }

    public String toSummaryString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(productTitle);
        sb.append(". ");
        sb.append("Reviewed by ");
        sb.append(author);
        sb.append(". ");
        sb.append(reviewSummary.toRateString());
        return sb.toString();
    }

    public String getProduct() {
        return product;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getMsrp() {
        return msrp;
    }

    public String getLowPrice() {
        return lowPrice;
    }

    public List<ProductSeller> getSellers() {
        return sellers;
    }

    public ReviewSummary getReviewSummary() {
        return reviewSummary;
    }

    public List<ProductSeller> getSellerAmazon() {
        return sellers.stream().filter(e -> e.getSeller().contains("Amazon")).collect(Collectors.toList());
    }
}
