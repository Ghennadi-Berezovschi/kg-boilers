package com.kgboilers.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class ProductionConfigurationCheckTest {
    @TempDir Path directory;

    private MockEnvironment settings() {
        return new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://database/app")
                .withProperty("spring.datasource.username", "app")
                .withProperty("spring.datasource.password", "test-db-password")
                .withProperty("spring.mail.host", "smtp.example.test")
                .withProperty("spring.mail.username", "test@example.test")
                .withProperty("spring.mail.password", "test-mail-password")
                .withProperty("kg.admin.username", "owner")
                .withProperty("kg.admin.password", "test-unique-admin-password")
                .withProperty("kg.uploads.quote-pictures-dir", directory.resolve("pictures").toString());
    }

    @Test void acceptsCompleteSettingsAndPreparesPictureDirectory() throws Exception {
        new ProductionConfigurationCheck(settings()).afterPropertiesSet();
        assertThat(directory.resolve("pictures")).isDirectory();
    }

    @Test void refusesMissingMailCredentialsWithoutPrintingSecrets() {
        var env = settings().withProperty("spring.mail.password", "");
        assertThatThrownBy(() -> new ProductionConfigurationCheck(env).afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("spring.mail.password")
                .hasMessageNotContaining("test-db-password");
    }

    @Test void refusesExampleAdminPassword() {
        var env = settings().withProperty("kg.admin.password", "change-this-admin-password");
        assertThatThrownBy(() -> new ProductionConfigurationCheck(env).afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("ADMIN_PASSWORD");
    }

    @Test void refusesEphemeralRelativePictureDirectory() {
        var env = settings().withProperty("kg.uploads.quote-pictures-dir", "uploads/quote-pictures");
        assertThatThrownBy(() -> new ProductionConfigurationCheck(env).afterPropertiesSet())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("persistent volume");
    }
}
