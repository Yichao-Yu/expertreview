package com.yichao.alexa.expertreview;

public final class SessionConstant {

    private SessionConstant() {
    }

    public static final String SESSION_FLOW_ENTRY = "session_flow_entry";
    public static final String SESSION_FLOW_STATE = "session_flow_state";
    public static final String SESSION_SEARCHED_PRODUCT = "session_searched_product";
    public static final String SESSION_LAST_RESPONSE = "session_last_response";
    public static final String SESSION_SEARCH_RESULTS = "session_search_results";
    public static final String SESSION_PROMPTED_REVIEW = "session_prompted_review";
    public static final String SESSION_REVIEW_DETAIL = "session_review_detail";

    public static final String ENTRY_PRODUCT_SEARCH = "entry_product_search";
    public static final String ENTRY_REVIEW_SEARCH = "entry_review_search";

    // review search flow states
    public static final String STATE_REVIEW_SEARCH_INTIAL = "state_review_search_initial";
    public static final String STATE_REVIEW_SEARCH_FIRST_HIT = "state_review_search_first_hit";
//    public static final String STATE_REVIEW_SEARCH_OTHER_HITS = "state_review_search_other_hits";
    public static final String STATE_REVIEW_SEARCH_SUMMARY = "state_review_search_summary";
    public static final String STATE_REVIEW_SEARCH_GOOD_BAD_BOTTOMLINE = "state_review_search_gbbl";
    public static final String STATE_REVIEW_SEARCH_LINK_CARD = "state_review_search_link_card";
    public static final String STATE_REVIEW_SEARCH_END = "state_review_search_end";

}
