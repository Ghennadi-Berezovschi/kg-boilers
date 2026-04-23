package com.kgboilers.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgboilers.config.properties.RequestLimitsProperties;
import com.kgboilers.dto.boilerinstallationquote.QuoteResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class QuoteRequestSizeFilter extends OncePerRequestFilter {

    private static final String REQUEST_TOO_LARGE_CODE = "REQUEST_TOO_LARGE";
    private static final String REQUEST_TOO_LARGE_MESSAGE = "Request is too large";

    private final RequestLimitsProperties requestLimitsProperties;
    private final ObjectMapper objectMapper;

    public QuoteRequestSizeFilter(RequestLimitsProperties requestLimitsProperties,
                                  ObjectMapper objectMapper) {
        this.requestLimitsProperties = requestLimitsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isQuoteApiRequest(request) && isRequestTooLarge(request)) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(
                    response.getWriter(),
                    QuoteResponseDto.builder()
                            .success(false)
                            .errorCode(REQUEST_TOO_LARGE_CODE)
                            .message(REQUEST_TOO_LARGE_MESSAGE)
                            .build()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isQuoteApiRequest(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        return path.startsWith("/quote/")
                || path.startsWith("/boiler-repair-quote/")
                || path.startsWith("/central-heating-quote/");
    }

    private boolean isRequestTooLarge(HttpServletRequest request) {
        long contentLength = request.getContentLengthLong();
        return contentLength > 0 && contentLength > requestLimitsProperties.maxContentLengthBytes();
    }
}
