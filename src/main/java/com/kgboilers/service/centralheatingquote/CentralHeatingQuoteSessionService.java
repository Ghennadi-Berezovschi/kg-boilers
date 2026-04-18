package com.kgboilers.service.centralheatingquote;

import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CentralHeatingQuoteSessionService {

    public static final String SESSION_KEY = "centralHeatingQuoteSessionState";
    public static final String SAVED_QUOTE_ID_KEY = "centralHeatingSavedQuoteId";

    public CentralHeatingQuoteSessionState getState(HttpSession session) {
        return (CentralHeatingQuoteSessionState) session.getAttribute(SESSION_KEY);
    }

    public CentralHeatingQuoteSessionState getOrCreateState(HttpSession session) {
        CentralHeatingQuoteSessionState state = getState(session);
        if (state == null) {
            state = new CentralHeatingQuoteSessionState();
            session.setAttribute(SESSION_KEY, state);
        }
        return state;
    }

    public void saveState(HttpSession session, CentralHeatingQuoteSessionState state) {
        session.setAttribute(SESSION_KEY, state);
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
        session.removeAttribute(SESSION_KEY);
        session.removeAttribute(SAVED_QUOTE_ID_KEY);
    }
}
