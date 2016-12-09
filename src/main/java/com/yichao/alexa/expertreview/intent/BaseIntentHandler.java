package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.Image;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.StandardCard;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yichao.alexa.expertreview.SpeechletResponseUtil;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.http.parser.CnetPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by yichaoyu on 11/30/16.
 */
public abstract class BaseIntentHandler implements IntentRequestHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(BaseIntentHandler.class);

    @Inject
    private ObjectMapper objectMapper;
    @Inject
    protected CnetPageClient cnetPageClient;
    @Inject
    protected CnetPageParser cnetPageParser;

    @Inject
    public void registerHandler(@Named("intentHandlerMap") final Map<IntentType, IntentRequestHandler> intentHandlerMap) {
        LOGGER.debug("Register handler {} to intentHandlerMap", getClass().getSimpleName());
        intentHandlerMap.put(getIntentType(), this);
    }

    protected abstract IntentType getIntentType();

    protected SpeechletResponse newTellResponse(final String stringOutput, final boolean isOutputSsml) {
        return newTellResponse(stringOutput, isOutputSsml, false);
    }

    protected SpeechletResponse newTellResponse(final String stringOutput, final boolean isOutputSsml, final boolean shouldEndSession) {
        LOGGER.debug("Response stringOutput: [{}]", stringOutput);
        final SpeechletResponse response = SpeechletResponseUtil.newTellResponse(stringOutput, isOutputSsml);
        response.setShouldEndSession(shouldEndSession);
        return response;
    }

    protected SpeechletResponse newTellResponseWithCard(final String stringOutput, final boolean isOutputSsml,
                                                        final String cardTitle, final String cardContent, final String imageUrl) {
        LOGGER.debug("Response stringOutput: [{}]", stringOutput);
        LOGGER.debug("Response card: [title: {}, content: {}]", cardTitle, cardContent);
        final Card card;
        if (imageUrl != null) {
            LOGGER.debug("Response card image: [url: {}]", imageUrl);
            card = new StandardCard();
            final Image smallImage = new Image();
            smallImage.setSmallImageUrl(imageUrl);
            ((StandardCard) card).setImage(smallImage);
            ((StandardCard) card).setText(cardContent);
        } else {
            card = new SimpleCard();
            ((SimpleCard) card).setContent(cardContent);
        }
        card.setTitle(cardTitle);
        final SpeechletResponse response = newTellResponse(stringOutput, isOutputSsml);
        response.setCard(card);
        response.setShouldEndSession(true); // assuming sending card is the end of session.
        return response;
    }

    protected SpeechletResponse newAskResponse(final String stringOutput, final boolean isOutputSsml,
                                               final String repromptText, final boolean isRepromptSsml) {
        LOGGER.debug("Response stringOutput: [{}]", stringOutput);
        return SpeechletResponseUtil.newAskResponse(stringOutput, isOutputSsml, repromptText, isRepromptSsml);
    }

    @Override
    public SpeechletResponse handleYesIntentRequest(Intent intent, Session session) throws SpeechletException {
        return newTellResponse("Not Implemented yet", false, true);
    }

    @Override
    public SpeechletResponse handleNoIntentRequest(Intent intent, Session session) throws SpeechletException {
        return newTellResponse("Not Implemented yet", false, true);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getSessionAttribute(final Session session, final String attributeName, final Class<?>... clazz) {
        final JavaType type = objectMapper.getTypeFactory().constructType(getParametericType(clazz));
        LOGGER.debug("Get {} type attribute {} from session", type.getRawClass(), attributeName);
        try {
            final String tmp = objectMapper.writeValueAsString(session.getAttribute(attributeName));
            return (T) objectMapper.readValue(tmp, type);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
