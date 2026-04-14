package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class QuoteResponseFactory {

    public ResponseEntity<QuoteResponseDto> success(QuoteStep nextStep) {
        return ResponseEntity.ok(
                QuoteResponseDto.builder()
                        .success(true)
                        .nextStep(nextStep.getPath())
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
}
