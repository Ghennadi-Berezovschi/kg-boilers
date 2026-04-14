package com.kgboilers.config.boilerinstallationquote;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final QuoteWizardInterceptor quoteWizardInterceptor;

    public WebConfig(QuoteWizardInterceptor quoteWizardInterceptor) {
        this.quoteWizardInterceptor = quoteWizardInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(quoteWizardInterceptor)
                .addPathPatterns("/quote/fuel-type", "/quote/property-ownership");
    }
}
