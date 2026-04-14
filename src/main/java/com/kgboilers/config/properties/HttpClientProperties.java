package com.kgboilers.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kg.http.client")
public record HttpClientProperties(
        int connectTimeout,
        int readTimeout
) {}
