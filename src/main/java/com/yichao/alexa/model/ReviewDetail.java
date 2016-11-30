package com.yichao.alexa.model;

public class ReviewDetail {

    private String product; // h1.headline > .itemreviewed
    private String productTitle; // h2.productTitle
    private String author; // div.metaData > .authors

    private ReviewSummary reviewSummary; // div.scoreCard

    public ReviewDetail() {
    }

    public ReviewDetail(String product, String productTitle, String author, ReviewSummary reviewSummary) {
        this.product = product;
        this.productTitle = productTitle;
        this.author = author;
        this.reviewSummary = reviewSummary;
    }

    public String toResponseString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<speak>");
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
        sb.append(reviewSummary.toResponseString());
        sb.append("</speak>");
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

    public ReviewSummary getReviewSummary() {
        return reviewSummary;
    }
}
