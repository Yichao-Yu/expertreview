package com.yichao.alexa.model;

public class ReviewSearchResult {

    private String url;
    private String title;
    private ReviewType reviewType;

    public ReviewSearchResult() {
    }

    public ReviewSearchResult(final String url, final String title, final ReviewType reviewType) {
        this.url = url;
        this.title = title;
        this.reviewType = reviewType;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public ReviewType getReviewType() {
        return reviewType;
    }

    @Override
    public String toString() {
        return "ReviewSearchResult{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", reviewType=" + reviewType +
                '}';
    }

}
