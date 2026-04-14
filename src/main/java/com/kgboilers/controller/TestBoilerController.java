package com.kgboilers.controller;

import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.service.boilerinstallationquote.BoilerCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestBoilerController {

    private final BoilerCatalogService boilerCatalogService;

    public TestBoilerController(BoilerCatalogService boilerCatalogService) {
        this.boilerCatalogService = boilerCatalogService;
    }

    @GetMapping("/test/boilers")
    public Map<String, List<BoilerModel>> testBoilers() {
        return Map.of(
                "combi", boilerCatalogService.getCombiBoilers(),
                "system", boilerCatalogService.getSystemBoilers(),
                "heatOnly", boilerCatalogService.getHeatOnlyBoilers()
        );
    }
}
