package com.yichao.alexa.expertreview.intent;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yichao.alexa.expertreview.SpeechletResponseUtil;
import com.yichao.alexa.http.client.CnetPageClient;
import com.yichao.alexa.http.parser.CnetSearchResultPageParser;
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
    protected CnetSearchResultPageParser cnetSearchResultPageParser;

    @Inject
    public void registerHandler(@Named("intentHandlerMap") final Map<IntentType, IntentRequestHandler> intentHandlerMap) {
        LOGGER.debug("Register handler {} to intentHandlerMap", getClass().getSimpleName());
        intentHandlerMap.put(getIntentType(), this);
    }

    protected abstract IntentType getIntentType();

    protected SpeechletResponse newTellResponse(final String stringOutput, final boolean isOutputSsml) {
        return SpeechletResponseUtil.newTellResponse(stringOutput, isOutputSsml);
    }

    protected SpeechletResponse newAskResponse(final String stringOutput, final boolean isOutputSsml,
                                               final String repromptText, final boolean isRepromptSsml) {
        return SpeechletResponseUtil.newAskResponse(stringOutput, isOutputSsml, repromptText, isRepromptSsml);
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
