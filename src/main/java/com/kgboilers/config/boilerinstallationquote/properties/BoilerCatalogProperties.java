package com.kgboilers.config.boilerinstallationquote.properties;

import com.kgboilers.model.boilerinstallationquote.BoilerModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "kg.boilers")
public class BoilerCatalogProperties {

    private List<BoilerModel> combi = new ArrayList<>();
    private List<BoilerModel> system = new ArrayList<>();
    private List<BoilerModel> heatOnly = new ArrayList<>();

    public List<BoilerModel> getCombi() {
        return combi;
    }

    public void setCombi(List<BoilerModel> combi) {
        this.combi = combi;
    }

    public List<BoilerModel> getSystem() {
        return system;
    }

    public void setSystem(List<BoilerModel> system) {
        this.system = system;
    }

    public List<BoilerModel> getHeatOnly() {
        return heatOnly;
    }

    public void setHeatOnly(List<BoilerModel> heatOnly) {
        this.heatOnly = heatOnly;
    }
}
