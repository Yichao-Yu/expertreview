package com.yichao.alexa.model;

/**
 * Created by yichaoyu on 11/26/16.
 */
public enum ReviewType {

    EDITOR_REVIEW("Editor Review"),
    USER_REVIEW("User Review");

    private String description;

    ReviewType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
