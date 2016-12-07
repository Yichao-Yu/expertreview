package com.yichao.alexa.model;

public class ReviewSearchResult {

    private String url;
    private String title;
    private ReviewType reviewType;
    private String imgUrl;

    public ReviewSearchResult() {
    }

    public ReviewSearchResult(final String url, final String title, final ReviewType reviewType, final String imgUrl) {
        this.url = url;
        this.title = title;
        this.reviewType = reviewType;
        this.imgUrl = imgUrl;
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

    public String getImgUrl() {
        return imgUrl;
    }

    @Override
    public String toString() {
        return "ReviewSearchResult{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", reviewType=" + reviewType +
                ", imgUrl=" + imgUrl +
                '}';
    }

}
