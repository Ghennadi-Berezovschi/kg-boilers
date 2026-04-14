package com.kgboilers.config.boilerinstallationquote;

import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class QuoteWizardInterceptor implements HandlerInterceptor {

    private final QuoteWizardService wizardService;

    public QuoteWizardInterceptor(QuoteWizardService wizardService) {
        this.wizardService = wizardService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession();

        if (!request.getMethod().equalsIgnoreCase("GET")) {
            return true;
        }

        QuoteSessionState state =
                (QuoteSessionState) session.getAttribute(QuoteSessionService.QUOTE_SESSION_STATE_KEY);

        if (uri.equals("/quote/fuel-type")) {
            if (!wizardService.canAccessStep(state, QuoteStep.FUEL_TYPE)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/property-ownership")) {
            if (!wizardService.canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP)) {
                response.sendRedirect("/quote/fuel-type");
                return false;
            }
        }

        if (uri.equals("/quote/property-type")) {
            if (!wizardService.canAccessStep(state, QuoteStep.PROPERTY_TYPE)) {
                response.sendRedirect("/quote/property-ownership");
                return false;
            }
        }

        return true;
    }
}
