package com.kgboilers.dto.boilerinstallationquote;

import com.kgboilers.model.boilerinstallation.enums.Bedrooms;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedroomsRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenBedroomsIsNull_thenValidationFails() {
        BedroomsRequestDto dto = new BedroomsRequestDto();
        dto.setBedrooms(null);

        Set<ConstraintViolation<BedroomsRequestDto>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void whenBedroomsIsNotNull_thenValidationSucceeds() {
        BedroomsRequestDto dto = new BedroomsRequestDto();
        dto.setBedrooms(Bedrooms.TWO);

        Set<ConstraintViolation<BedroomsRequestDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        BedroomsRequestDto dto = new BedroomsRequestDto();
        dto.setBedrooms(Bedrooms.SIX_PLUS);

        assertEquals(Bedrooms.SIX_PLUS, dto.getBedrooms());
    }
}
