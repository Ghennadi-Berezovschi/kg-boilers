package com.kgboilers.web.common;

import com.kgboilers.config.properties.ContactProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final ContactProperties contactProperties;

    public GlobalModelAttributes(ContactProperties contactProperties) {
        this.contactProperties = contactProperties;
    }

    @ModelAttribute("contact")
    public ContactProperties contact() {
        return contactProperties;
    }
}
