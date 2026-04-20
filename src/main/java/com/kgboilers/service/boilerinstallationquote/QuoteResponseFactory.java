package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class QuoteResponseFactory {

    private static final String BOILER_REPAIR_SERVICE = "boiler-repair";

    public ResponseEntity<QuoteResponseDto> success(QuoteStep nextStep) {
        return success(nextStep, null);
    }

    public ResponseEntity<QuoteResponseDto> success(QuoteStep nextStep, String service) {
        return ResponseEntity.ok(
                QuoteResponseDto.builder()
                        .success(true)
                        .nextStep(resolveNextStepPath(nextStep, service))
                        .build()
        );
    }

    public ResponseEntity<QuoteResponseDto> error(String code, String message) {
        return ResponseEntity.ok(
                QuoteResponseDto.builder()
                        .success(false)
                        .errorCode(code)
                        .message(message)
                        .build()
        );
    }

    public ResponseEntity<QuoteResponseDto> badRequest(String code, String message) {
        return ResponseEntity.badRequest()
                .body(
                        QuoteResponseDto.builder()
                                .success(false)
                                .errorCode(code)
                                .message(message)
                                .build()
                );
    }

    private String resolveNextStepPath(QuoteStep nextStep, String service) {
        if (!BOILER_REPAIR_SERVICE.equalsIgnoreCase(service == null ? "" : service.trim())) {
            return nextStep.getPath();
        }

        if (nextStep == QuoteStep.START) {
            return "/boiler-repair-quote";
        }

        return nextStep.getPath().replaceFirst("^/quote", "/boiler-repair-quote");
    }
}
