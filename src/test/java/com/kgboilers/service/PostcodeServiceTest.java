package com.kgboilers.service;

import com.kgboilers.exception.ExternalServiceException;
import com.kgboilers.exception.PostcodeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostcodeServiceTest {

    private RestTemplate restTemplate;
    private PostcodeService postcodeService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        postcodeService = new PostcodeService(restTemplate);
    }

    @Test
    void shouldThrowPostcodeNotFoundExceptionOn404() {
        when(restTemplate.getForObject(anyString(), eq(com.kgboilers.dto.PostcodeApiResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(PostcodeNotFoundException.class, () -> postcodeService.getCoordinates("INVALID"));
    }

    @Test
    void shouldThrowExternalServiceExceptionOn500() {
        when(restTemplate.getForObject(anyString(), eq(com.kgboilers.dto.PostcodeApiResponse.class)))
                .thenThrow(new RuntimeException("API Down"));

        assertThrows(ExternalServiceException.class, () -> postcodeService.getCoordinates("SW1A1AA"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionOnEmptyPostcode() {
        assertThrows(IllegalArgumentException.class, () -> postcodeService.getCoordinates(""));
        assertThrows(IllegalArgumentException.class, () -> postcodeService.getCoordinates(null));
    }
}
