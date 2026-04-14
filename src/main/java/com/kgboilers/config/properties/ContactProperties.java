package com.kgboilers.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kg.contact")
public class ContactProperties {

    private String email;
    private String phone;
    private String phoneDisplay;
    private String whatsapp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneDisplay() {
        return phoneDisplay;
    }

    public void setPhoneDisplay(String phoneDisplay) {
        this.phoneDisplay = phoneDisplay;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}