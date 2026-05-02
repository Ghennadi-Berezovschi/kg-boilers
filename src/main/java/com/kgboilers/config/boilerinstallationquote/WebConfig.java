package com.kgboilers.config.boilerinstallationquote;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final QuoteWizardInterceptor quoteWizardInterceptor;
    private final String quotePicturesDirectory;

    public WebConfig(QuoteWizardInterceptor quoteWizardInterceptor,
                     @Value("${kg.uploads.quote-pictures-dir}") String quotePicturesDirectory) {
        this.quoteWizardInterceptor = quoteWizardInterceptor;
        this.quotePicturesDirectory = quotePicturesDirectory;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(quoteWizardInterceptor)
                .addPathPatterns(
                        "/quote/service-type",
                        "/quote/fuel-type",
                        "/quote/property-ownership",
                        "/quote/property-type",
                        "/quote/boiler-type",
                        "/quote/boiler-make"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(quotePicturesDirectory).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/quote-pictures/**")
                .addResourceLocations(location);
    }
}
