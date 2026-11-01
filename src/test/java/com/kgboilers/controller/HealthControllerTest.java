package com.kgboilers.controller;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HealthControllerTest {
    @Test void reportsReadyWhenDatabaseResponds() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Map.of(), Integer.class)).thenReturn(1);
        var result = new HealthController(jdbc).health();
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getHeaders().getCacheControl()).contains("no-store");
    }

    @Test void reportsUnavailableWithoutDisclosingDatabaseErrors() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Map.of(), Integer.class))
                .thenThrow(new DataAccessResourceFailureException("private database details"));
        var result = new HealthController(jdbc).health();
        assertThat(result.getStatusCode().value()).isEqualTo(503);
        assertThat(result.getBody()).containsEntry("status", "unavailable");
    }
}
