package com.kgboilers.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kg.http.request")
public record RequestLimitsProperties(
        int maxContentLengthBytes
) {}
