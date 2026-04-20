package com.kgboilers.web.common;

import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.config.properties.CompanyProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final ContactProperties contactProperties;
    private final CompanyProperties companyProperties;

    public GlobalModelAttributes(ContactProperties contactProperties,
                                 CompanyProperties companyProperties) {
        this.contactProperties = contactProperties;
        this.companyProperties = companyProperties;
    }

    @ModelAttribute("contact")
    public ContactProperties contact() {
        return contactProperties;
    }

    @ModelAttribute("company")
    public CompanyProperties company() {
        return companyProperties;
    }
}
