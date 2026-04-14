package com.kgboilers.dto.boilerinstallationquote;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SW1A 1AA", "E16 4JJ", "W1A 0AX", "M1 1AE", "B33 8TH", "CR2 6XH", "DN55 1PT", "sw1a 1aa", "E164JJ", "12345", "ABC DE"})
    void shouldValidatePostcodes(String postcode) {
        QuoteRequestPostcodeDto request = createValidRequest();
        request.setPostcode(postcode);

        Set<ConstraintViolation<QuoteRequestPostcodeDto>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Postcode should be valid (not strictly formatted): " + postcode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void shouldNotValidateEmptyPostcodes(String postcode) {
        QuoteRequestPostcodeDto request = createValidRequest();
        request.setPostcode(postcode);

        Set<ConstraintViolation<QuoteRequestPostcodeDto>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Postcode should be invalid if empty");
    }

    private QuoteRequestPostcodeDto createValidRequest() {
        QuoteRequestPostcodeDto request = new QuoteRequestPostcodeDto();

        return request;
    }
}
