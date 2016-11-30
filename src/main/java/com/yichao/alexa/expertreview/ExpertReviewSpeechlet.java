package com.yichao.alexa.expertreview;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.http.parser.CnetSearchResultPageParser;
import com.yichao.alexa.model.ReviewDetail;
import com.yichao.alexa.model.ReviewSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class ExpertReviewSpeechlet implements Speechlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpertReviewSpeechlet.class);

    private static final String INTENT_PRODUCT_SEARCH = "ProductSearchIntent";
    private static final String INTENT_PRODUCT_REVIEW_SEARCH = "ProductReviewSearchIntent";
    private static final String INTENT_OTHER_REVIEW = "OtherReviewResultIntent";
    private static final String INTENT_BUILTIN_YES = "AMAZON.YesIntent";
    private static final String INTENT_BUILTIN_REPEAT = "AMAZON.RepeatIntent";

    private static final String SESSION_SEARCHED_PRODUCT = "session_searched_product";
    private static final String SESSION_LAST_RESPONSE = "session_last_response";
    private static final String SESSION_SEARCH_RESULTS = "session_search_results";
    private static final String SESSION_PROMPTED_SEARCH_RESULT = "session_prompted_search_result";

    private static final int productSearchResultLimit = 5;

    ExpertReviewSpeechlet(final Injector injector) {
        injector.injectMembers(this);
    }

    @Inject
    private CnetPageClient cnetPageClient;

    @Inject
    private CnetSearchResultPageParser cnetSearchResultPageParser;

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        LOGGER.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        resetSearchAttributes(session);
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        LOGGER.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        LOGGER.info("onIntent {}, requestId={}, sessionId={}", request.getIntent().getName(), request.getRequestId(), session.getSessionId());

        final Intent intent = request.getIntent();
        final String intentName = intent.getName();

        if (INTENT_PRODUCT_SEARCH.equals(intentName)) {
            return handleProductSearchIntent(intent, session);
        } else if (INTENT_PRODUCT_REVIEW_SEARCH.equals(intentName)) {
            return handleReviewSearchIntent(intent, session);
        } else if (INTENT_BUILTIN_YES.equals(intentName)) {
            return handleYesIntent(intent, session);
        } else if (INTENT_OTHER_REVIEW.equals(intentName)) {
            return handleOtherReviewIntent(intent, session);
        } else if (INTENT_BUILTIN_REPEAT.equals(intentName)) {
            return handleRepeatIntent(intent, session);
        }
        throw new SpeechletException("Invalid Intent");
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        LOGGER.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
    }

    private SpeechletResponse handleReviewSearchIntent(final Intent intent, final Session session) {
        LOGGER.info("Handling review search");
        final String product = intent.getSlot("Product").getValue();
        LOGGER.debug("product is {}", product);

        if (!session.getAttributes().containsKey(SESSION_SEARCHED_PRODUCT) // no searched product
                || !getSessionAttribute(session, SESSION_SEARCHED_PRODUCT, String.class).equals(product)) { // different searched product
            // new search
            resetSearchAttributes(session);
            session.setAttribute(SESSION_SEARCHED_PRODUCT, product);
        }

        final List<ReviewSearchResult> results;
        if (session.getAttributes().containsKey(SESSION_SEARCH_RESULTS)) {
            LOGGER.debug("Load cached results from session attribute {}", SESSION_SEARCH_RESULTS);
            // search was done on the same product, read from cache
            results = getSessionAttribute(session, SESSION_SEARCH_RESULTS, List.class, ReviewSearchResult.class);
        } else {
            LOGGER.debug("Search product {}", product);
            // search reviews
            final String searchPage = cnetPageClient.getSearchResultPage(product);
            results = cnetSearchResultPageParser.parseSearchResult(searchPage);
            session.setAttribute(SESSION_SEARCH_RESULTS, results);
        }

        final ReviewSearchResult firstHit = results.get(0);
        session.setAttribute(SESSION_PROMPTED_SEARCH_RESULT, firstHit);
        session.removeAttribute(SESSION_LAST_RESPONSE);
        final String responseString = "Find " + firstHit.getReviewType().getDescription() + ": " + firstHit.getTitle()
                + ". Want to learn the review?";

        LOGGER.debug("Response string: [{}]", responseString);
        return newAskResponse(responseString, false, "", false);
    }

    private SpeechletResponse handleProductSearchIntent(final Intent intent, final Session session) {
        LOGGER.info("Handling product search");
        final String product = intent.getSlot("Product").getValue();
        LOGGER.debug("product is {}", product);

        // new search
        resetSearchAttributes(session);
        session.setAttribute(SESSION_SEARCHED_PRODUCT, product);

        final List<ReviewSearchResult> results;
        LOGGER.debug("Search product {}", product);
        // search reviews
        final String searchPage = cnetPageClient.getSearchResultPage(product);
        results = cnetSearchResultPageParser.parseSearchResult(searchPage);
        session.setAttribute(SESSION_SEARCH_RESULTS, results);

        final int found = getResultSetSize(results);
        if (found == 0) {
            LOGGER.info("No product {} was found", product);
            return newTellResponse("Sorry. I did not find any product called " + product, false);
        }
        String responseString = "Find " + found + " results: ";
        for (int i = 0; i < found; i++) {
            responseString = responseString + "No." + (i + 1) + ": " + results.get(i).getTitle() + ". ";
        }
        responseString += "Which one is the one you wanted?";

        LOGGER.debug("Response string: [{}]", responseString);
        session.setAttribute(SESSION_LAST_RESPONSE, responseString);
        return newAskResponse(responseString, false, "", false);
    }

    private SpeechletResponse handleReviewDetail(final Intent intent, final Session session) {
        LOGGER.info("Handling review detail");
        final ReviewSearchResult searchResult = getSessionAttribute(session, SESSION_PROMPTED_SEARCH_RESULT, ReviewSearchResult.class);

        final String reviewPage = cnetPageClient.getReviewPage(searchResult.getUrl());
        final ReviewDetail reviewDetail = cnetSearchResultPageParser.parseReviewDetail(reviewPage);

        final String responseString = reviewDetail.toResponseString();
        LOGGER.debug("Response string: [{}]", responseString);
        session.setAttribute(SESSION_LAST_RESPONSE, responseString);
        return newTellResponse(responseString, true);
    }

    private SpeechletResponse handleYesIntent(final Intent intent, final Session session) {
        if (session.getAttributes().containsKey(SESSION_PROMPTED_SEARCH_RESULT)) {
            return handleReviewDetail(intent, session);
        } else {
            return newTellResponse("No implemented yet", false);
        }
    }

    private SpeechletResponse handleOtherReviewIntent(final Intent intent, final Session session) {
        if (session.getAttributes().containsKey(SESSION_PROMPTED_SEARCH_RESULT)) {
            final List<ReviewSearchResult> results =
                    getSessionAttribute(session, SESSION_SEARCH_RESULTS, List.class, ReviewSearchResult.class);
            if (results == null || results.isEmpty()) {
                return newTellResponse("Sorry, I did not find any other review.", false);
            } else {
                final int found = getResultSetSize(results);
                String responseString = "Okay. Here are other reviews related to : "
                        + getSessionAttribute(session, SESSION_PROMPTED_SEARCH_RESULT, String.class);
                for (int i = 1; i < found; i++) {
                    responseString = responseString + "No." + (i - 1) + ": " + results.get(i).getTitle() + ". ";
                }
                responseString += "Which one is the one you wanted?";

                LOGGER.debug("Response string: [{}]", responseString);
                session.setAttribute(SESSION_LAST_RESPONSE, responseString);
                return newAskResponse(responseString, false, "", false);
            }
        } else {
            return newTellResponse("No implemented yet", false);
        }
    }

    private SpeechletResponse handleRepeatIntent(final Intent intent, final Session session) {
        if (session.getAttributes().containsKey(SESSION_LAST_RESPONSE)) {
            return getSessionAttribute(session, SESSION_LAST_RESPONSE, String.class);
        } else {
            return newTellResponse("No previous search. Please tell me what product you want to know the review.", false);
        }
    }

    private SpeechletResponse getWelcomeResponse() {
        String speechOutput = "Welcome to Expert Review. "
                + "Which product would you like to hear review for?";
        String repromptText =
                "I can find you a product review from an professional review site. "
                        + "or if you don't know the full name of the product you want to look up review for, "
                        + "you can say 'Help me find iPhone'. Then you can choose one from the list of products found.";

        return newAskResponse(speechOutput, false, repromptText, false);
    }

    private SpeechletResponse newTellResponse(final String stringOutput, final boolean isOutputSsml) {
        final OutputSpeech outputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }
        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    private SpeechletResponse newAskResponse(final String stringOutput, final boolean isOutputSsml,
                                             final String repromptText, final boolean isRepromptSsml) {
        final OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, new Reprompt());
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(final Session session, final String attributeName, final Class<?>... clazz) {
        final JavaType type = objectMapper.getTypeFactory().constructType(getParametericType(clazz));
        LOGGER.debug("Get {} type attribute {} from session", type.getRawClass(), attributeName);
        try {
            final String tmp = objectMapper.writeValueAsString(session.getAttribute(attributeName));
            return (T) objectMapper.readValue(tmp, type);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void resetSearchAttributes(final Session session) {
        LOGGER.debug("Reset Search attributes in session");
        session.removeAttribute(SESSION_LAST_RESPONSE);
        session.removeAttribute(SESSION_SEARCHED_PRODUCT);
        session.removeAttribute(SESSION_SEARCH_RESULTS);
        session.removeAttribute(SESSION_PROMPTED_SEARCH_RESULT);
    }

    private int getResultSetSize(List<?> results) {
        return productSearchResultLimit <= 0 ? results.size()
                : results.size() < productSearchResultLimit ? results.size() : productSearchResultLimit;
    }

    private Type getParametericType(Class<?>[] params) {
        for (Class<?> c : params) {
            if (c == null) {
                throw new RuntimeException("Null value passed as class params");
            }
        }
        if (params.length == 1) {
            return params[0];
        }
        int i = params.length - 1;
        Type paramType = params[i--];
        ParameterizedType pType = new SessionAttributeParameterizedType(params[i--], paramType);
        while (i >= 0) {
            pType = new SessionAttributeParameterizedType(params[i--], pType);
        }
        return pType;
    }

    private static class SessionAttributeParameterizedType implements ParameterizedType {

        Type rawType;
        Type paramType;

        SessionAttributeParameterizedType(Type rawType, Type paramType) {
            this.rawType = rawType;
            this.paramType = paramType;
        }

        public Type getRawType() {
            return rawType;
        }

        public Type getOwnerType() {
            return null;
        }

        public Type[] getActualTypeArguments() {
            return new Type[]{paramType};
        }
    }
}
