package com.kgboilers.service;

import com.kgboilers.config.CacheConfig;
import com.kgboilers.model.Coordinates;
import com.kgboilers.dto.PostcodeApiResponse;
import com.kgboilers.exception.ExternalServiceException;
import com.kgboilers.exception.PostcodeNotFoundException;

import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class PostcodeService {

    private static final Logger log = LoggerFactory.getLogger(PostcodeService.class);

    private final RestTemplate restTemplate;

    public PostcodeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable(
            value = CacheConfig.CACHE_POSTCODES,
            key = "#postcode?.replace(' ', '')?.toUpperCase()",
            unless = "#result == null"
    )
    @Retry(name = "postcodeApi", fallbackMethod = "fallback")
    @CircuitBreaker(name = "postcodeApi", fallbackMethod = "fallback")
    public Coordinates getCoordinates(String postcode) {

        if (postcode == null || postcode.isBlank()) {
            throw new IllegalArgumentException("Postcode cannot be empty");
        }

        String cleanPostcode = postcode.replace(" ", "").toUpperCase();

        String url = String.format(
                "https://api.postcodes.io/postcodes/%s",
                cleanPostcode
        );

        log.debug("Calling postcode API: {}", url);

        try {
            PostcodeApiResponse response = callApi(url);

            if (response == null || response.getResult() == null) {
                log.warn("Invalid postcode response received from postcode service");
                throw new PostcodeNotFoundException("Invalid postcode");
            }

            return new Coordinates(
                    response.getResult().getLatitude(),
                    response.getResult().getLongitude()
            );

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == org.springframework.http.HttpStatus.NOT_FOUND) {
                log.warn("Postcode was not found by postcode service");
                throw new PostcodeNotFoundException("Postcode not found");
            }

            log.error("Client error while calling postcode API", e);
            throw new ExternalServiceException("Postcode service client error", e);

        } catch (Exception e) {
            log.error("Error calling postcode API", e);
            throw new ExternalServiceException("Postcode service is currently unavailable", e);
        }
    }

    private PostcodeApiResponse callApi(String url) {
        log.debug("Executing RestTemplate call to: {}", url);
        return restTemplate.getForObject(url, PostcodeApiResponse.class);
    }

    /**
     * Fallback triggered when:
     * 1. CircuitBreaker is OPEN.
     * 2. Retries are exhausted (Retry is inside CB).
     * 3. An ignored exception (like PostcodeNotFoundException) is thrown.
     */
    public Coordinates fallback(String postcode, Throwable t) throws Throwable {

        if (t instanceof PostcodeNotFoundException) {
            throw t;
        }

        log.error("Postcode service fallback triggered: {}", t.getClass().getSimpleName());

        if (t instanceof ExternalServiceException) {
            throw t;
        }

        throw new ExternalServiceException(
                "Postcode service failed after retries or circuit opened",
                t
        );
    }
}
