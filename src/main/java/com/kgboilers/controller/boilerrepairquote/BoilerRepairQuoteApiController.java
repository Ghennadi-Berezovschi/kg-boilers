package com.kgboilers.controller.boilerrepairquote;

import com.kgboilers.dto.boilerinstallationquote.QuoteRequestPostcodeDto;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import com.kgboilers.model.boilerinstallationquote.QuoteSessionState;
import com.kgboilers.service.boilerinstallationquote.QuoteResponseFactory;
import com.kgboilers.service.boilerinstallationquote.QuoteService;
import com.kgboilers.service.boilerinstallationquote.QuoteSessionService;
import com.kgboilers.service.boilerinstallationquote.QuoteWizardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/boiler-repair-quote")
public class BoilerRepairQuoteApiController {

    private final QuoteService quoteService;
    private final QuoteWizardService wizardService;
    private final QuoteSessionService sessionService;
    private final QuoteResponseFactory responseFactory;

    public BoilerRepairQuoteApiController(QuoteService quoteService,
                                          QuoteWizardService wizardService,
                                          QuoteSessionService sessionService,
                                          QuoteResponseFactory responseFactory) {
        this.quoteService = quoteService;
        this.wizardService = wizardService;
        this.sessionService = sessionService;
        this.responseFactory = responseFactory;
    }

    @PostMapping("/start")
    public ResponseEntity<QuoteResponseDto> startQuote(@RequestBody @Valid QuoteRequestPostcodeDto request,
                                                       HttpSession session) {
        String postcode = request.getPostcode().trim().toUpperCase();
        quoteService.startQuote(postcode, "boiler-repair");

        session.setAttribute("service", "boiler-repair");
        sessionService.clearState(session);
        QuoteSessionState state = sessionService.getOrCreateState(session);
        QuoteStep nextStep = wizardService.startWizard(state, postcode);
        sessionService.saveState(session, state);

        log.info("Boiler repair quote started");

        return responseFactory.success(nextStep, "boiler-repair");
    }
}
