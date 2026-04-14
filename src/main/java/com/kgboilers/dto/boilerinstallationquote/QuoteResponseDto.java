package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.QuoteStep;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuoteResponseDto {

    private boolean success;
    private String errorCode;
    private String message;
    private String nextStep;
}