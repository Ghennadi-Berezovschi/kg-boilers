package com.kgboilers.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Fail clearly before accepting production traffic with incomplete settings. */
@Component
@Profile("prod")
public class ProductionConfigurationCheck implements InitializingBean {
    private final Environment environment;

    public ProductionConfigurationCheck(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        for (String key : List.of("spring.datasource.url", "spring.datasource.username", "spring.datasource.password",
                "spring.mail.host", "spring.mail.username", "spring.mail.password", "kg.admin.username")) {
            String value = environment.getProperty(key);
            if (value == null || value.isBlank() || value.toLowerCase(Locale.ROOT).contains("change_me")) {
                throw new IllegalStateException("Production setting is missing: " + key);
            }
        }
        String password = environment.getProperty("kg.admin.password", "");
        if (password.length() < 16 || password.toLowerCase(Locale.ROOT).contains("change")) {
            throw new IllegalStateException("Set a unique ADMIN_PASSWORD of at least 16 characters for production.");
        }
        String configured = environment.getProperty("kg.uploads.quote-pictures-dir", "");
        if (configured.isBlank() || !Path.of(configured).isAbsolute()) {
            throw new IllegalStateException("QUOTE_PICTURES_DIR must point to an absolute persistent volume path in production.");
        }
        Path directory = Path.of(configured);
        Files.createDirectories(directory);
        if (!Files.isWritable(directory)) {
            throw new IllegalStateException("The production picture volume is not writable.");
        }
    }
}
