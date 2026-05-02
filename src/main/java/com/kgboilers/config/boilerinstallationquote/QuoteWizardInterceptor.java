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

    private static final String BOILER_INSTALLATION_SERVICE = "boiler-installation";
    private static final String HOT_WATER_CYLINDER_SERVICE = "hot-water-cylinder";

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
        String service = getSelectedService(session);

        if (uri.equals("/quote/service-type")) {
            if (!canAccessStep(state, QuoteStep.SERVICE_TYPE, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/fuel-type")) {
            if (shouldSkipFuel(service) && canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, service)) {
                response.sendRedirect("/quote/property-ownership");
                return false;
            }

            if (!canAccessStep(state, QuoteStep.FUEL_TYPE, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/property-ownership")) {
            if (!canAccessStep(state, QuoteStep.PROPERTY_OWNERSHIP, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/property-type")) {
            if (!canAccessStep(state, QuoteStep.PROPERTY_TYPE, service)) {
                response.sendRedirect("/quote/property-ownership");
                return false;
            }
        }

        if (uri.equals("/quote/boiler-type")) {
            if (!canAccessStep(state, QuoteStep.BOILER_TYPE, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/boiler-make")) {
            if (!canAccessStep(state, QuoteStep.BOILER_MAKE, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/hot-water")) {
            if (!canAccessStep(state, QuoteStep.HOT_WATER, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        if (uri.equals("/quote/problem-details")) {
            if (!canAccessStep(state, QuoteStep.PROBLEM_DETAILS, service)) {
                response.sendRedirect("/quote");
                return false;
            }
        }

        return true;
    }

    private boolean canAccessStep(QuoteSessionState state, QuoteStep step, String service) {
        return isDefaultInstallationService(service)
                ? wizardService.canAccessStep(state, step)
                : wizardService.canAccessStep(state, step, service);
    }

    private String getSelectedService(HttpSession session) {
        Object service = session.getAttribute("service");
        if (service instanceof String serviceValue && !serviceValue.isBlank()) {
            return serviceValue.trim().toLowerCase();
        }

        return BOILER_INSTALLATION_SERVICE;
    }

    private boolean isDefaultInstallationService(String service) {
        return BOILER_INSTALLATION_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }

    private boolean shouldSkipFuel(String service) {
        return HOT_WATER_CYLINDER_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim());
    }
}
