package com.kgboilers.service.centralheatingquote;

import com.kgboilers.model.centralheatingquote.CentralHeatingQuoteSessionState;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CentralHeatingQuoteSessionService {

    public static final String SESSION_KEY = "centralHeatingQuoteSessionState";

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

    public void clearState(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }
}
