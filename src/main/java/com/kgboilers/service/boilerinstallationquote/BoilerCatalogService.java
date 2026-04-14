package com.kgboilers.service.boilerinstallationquote;

import com.kgboilers.config.boilerinstallationquote.properties.BoilerCatalogProperties;
import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import com.kgboilers.model.boilerinstallation.enums.BoilerType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoilerCatalogService {

    private final BoilerCatalogProperties boilerCatalogProperties;

    public BoilerCatalogService(BoilerCatalogProperties boilerCatalogProperties) {
        this.boilerCatalogProperties = boilerCatalogProperties;
    }

    public List<BoilerModel> getCombiBoilers() {
        return boilerCatalogProperties.getCombi();
    }

    public List<BoilerModel> getSystemBoilers() {
        return boilerCatalogProperties.getSystem();
    }

    public List<BoilerModel> getHeatOnlyBoilers() {
        return boilerCatalogProperties.getHeatOnly();
    }

    public List<BoilerModel> getBoilersForType(BoilerType boilerType) {
        if (boilerType == null) {
            return List.of();
        }

        return switch (boilerType) {
            case COMBI -> getCombiBoilers();
            case SYSTEM -> getSystemBoilers();
            case HEAT_ONLY -> getHeatOnlyBoilers();
            case OTHER -> List.of();
        };
    }
}
