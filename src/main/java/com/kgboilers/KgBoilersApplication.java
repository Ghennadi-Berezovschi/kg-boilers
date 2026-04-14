package com.kgboilers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KgBoilersApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgBoilersApplication.class, args);
    }
}