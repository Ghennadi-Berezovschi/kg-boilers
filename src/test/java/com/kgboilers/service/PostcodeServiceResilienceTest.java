package com.kgboilers.service;

import com.kgboilers.dto.PostcodeApiResponse;
import com.kgboilers.exception.ExternalServiceException;
import com.kgboilers.exception.PostcodeNotFoundException;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class PostcodeServiceResilienceTest {

    @MockBean
    private org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate jdbcTemplate;

    @MockBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @Autowired
    private PostcodeService postcodeService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetState() {
        circuitBreakerRegistry.circuitBreaker("postcodeApi").reset();
        reset(restTemplate);
    }

    @Test
    void shouldRetryThreeTimesAndCallFallback() {

        when(restTemplate.getForObject(anyString(), eq(PostcodeApiResponse.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> postcodeService.getCoordinates("E164JJ")
        );

        // max-attempts = 3
        verify(restTemplate, times(3))
                .getForObject(anyString(), eq(PostcodeApiResponse.class));
    }

    /**
     * Проверяем:
     * - 404 НЕ вызывает retry
     */
    @Test
    void shouldNotRetryOnPostcodeNotFoundException() {

        when(restTemplate.getForObject(anyString(), eq(PostcodeApiResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(PostcodeNotFoundException.class,
                () -> postcodeService.getCoordinates("INVALID"));

        verify(restTemplate, times(1))
                .getForObject(anyString(), eq(PostcodeApiResponse.class));
    }

    /**
     * Проверяем:
     * - CircuitBreaker открывается
     */
    @Test
    void shouldOpenCircuitBreakerAfterFailures() {

        CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker("postcodeApi");

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        when(restTemplate.getForObject(anyString(), eq(PostcodeApiResponse.class)))
                .thenThrow(new ResourceAccessException("timeout"));

        // вызываем несколько раз, чтобы накопить ошибки
        for (int i = 0; i < 5; i++) {
            assertThrows(ExternalServiceException.class,
                    () -> postcodeService.getCoordinates("E164JJ"));
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // после открытия CB вызов НЕ должен идти в RestTemplate
        int callsBefore = mockingDetails(restTemplate)
                .getInvocations().size();

        assertThrows(ExternalServiceException.class,
                () -> postcodeService.getCoordinates("E164JJ"));

        int callsAfter = mockingDetails(restTemplate)
                .getInvocations().size();

        // количество вызовов НЕ увеличилось
        assertEquals(callsBefore, callsAfter);
    }
}
