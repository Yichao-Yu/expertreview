package com.yichao.alexa.expertreview.intent;

/**
 * Created by yichaoyu on 11/30/16.
 */
public enum IntentType {
    PRODUCT_REVIEW_SEARCH("ProductReviewSearchIntent"),
    BUILTIN_YES("AMAZON.YesIntent"),
    BUILTIN_NO("AMAZON.NoIntent"),
    BUILTIN_REPEAT("AMAZON.RepeatIntent");

    private String intent;

    IntentType(String intent) {
        this.intent = intent;
    }

    public static IntentType fromValue(final String intent) {
        if (intent == null) {
            return null;
        }
        for (final IntentType i : values()) {
            if (i.intent.equals(intent)) {
                return i;
            }
        }
        return null;
    }
}
