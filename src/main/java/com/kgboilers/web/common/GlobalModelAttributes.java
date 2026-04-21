package com.kgboilers.web.common;

import com.kgboilers.config.properties.ContactProperties;
import com.kgboilers.config.properties.BoilerRepairProperties;
import com.kgboilers.config.properties.CompanyProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final ContactProperties contactProperties;
    private final CompanyProperties companyProperties;
    private final BoilerRepairProperties boilerRepairProperties;

    public GlobalModelAttributes(ContactProperties contactProperties,
                                 CompanyProperties companyProperties,
                                 BoilerRepairProperties boilerRepairProperties) {
        this.contactProperties = contactProperties;
        this.companyProperties = companyProperties;
        this.boilerRepairProperties = boilerRepairProperties;
    }

    @ModelAttribute("contact")
    public ContactProperties contact() {
        return contactProperties;
    }

    @ModelAttribute("company")
    public CompanyProperties company() {
        return companyProperties;
    }

    @ModelAttribute("boilerRepair")
    public BoilerRepairProperties boilerRepair() {
        return boilerRepairProperties;
    }
}
