package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class QuoteSessionService {

    public static final String QUOTE_SESSION_STATE_KEY = "quoteSessionState";
    public static final String SAVED_QUOTE_ID_KEY = "savedQuoteId";

    public QuoteSessionState getState(HttpSession session) {
        return (QuoteSessionState) session.getAttribute(QUOTE_SESSION_STATE_KEY);
    }

    public QuoteSessionState getOrCreateState(HttpSession session) {
        QuoteSessionState state = getState(session);
        if (state == null) {
            state = new QuoteSessionState();
            session.setAttribute(QUOTE_SESSION_STATE_KEY, state);
        }
        return state;
    }

    public void saveState(HttpSession session, QuoteSessionState state) {
        session.setAttribute(QUOTE_SESSION_STATE_KEY, state);
    }

    public Long getSavedQuoteId(HttpSession session) {
        Object value = session.getAttribute(SAVED_QUOTE_ID_KEY);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public void saveSavedQuoteId(HttpSession session, Long quoteId) {
        session.setAttribute(SAVED_QUOTE_ID_KEY, quoteId);
    }

    public void clearState(HttpSession session) {
        session.removeAttribute(QUOTE_SESSION_STATE_KEY);
        session.removeAttribute(SAVED_QUOTE_ID_KEY);
    }
}
