package com.kgboilers.model.boilerinstallation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoilerMake {

    WORCESTER_BOSCH("worcester-bosch", "Worcester Bosch", "WB", "#e04f43", "/images/boiler-makes/worcester-bosch.png"),
    IDEAL("ideal", "Ideal", "ideal", "#8a7550", "/images/boiler-makes/ideal.png"),
    VAILLANT("vaillant", "Vaillant", "V", "#4fa46f", "/images/boiler-makes/vaillant.png"),
    BAXI("baxi", "Baxi", "BAXI", "#2d6fbe", "/images/boiler-makes/baxi.png"),
    BIASI("biasi", "Biasi", "B", "#ed3b3b", "/images/boiler-makes/biasi.svg"),
    ELM_LEBLANC("elm-leblanc", "Elm Leblanc", "EL", "#2f6da5", "/images/boiler-makes/elm-leblanc.png"),
    GLOW_WORM("glow-worm", "Glow-worm", "GW", "#ff7a59", "/images/boiler-makes/glow-worm.png"),
    ALPHA("alpha", "Alpha", "Alpha", "#ec5d53"),
    ARISTON("ariston", "Ariston", "A", "#d7473f", "/images/boiler-makes/ariston.svg"),
    ATAG("atag", "Atag", "ATAG", "#2d6fbe"),
    FERROLI("ferroli", "Ferroli", "F", "#1f2f57"),
    HEATLINE("heatline", "Heatline", "HL", "#475569", "/images/boiler-makes/heatline.svg"),
    INTERGAS("intergas", "Intergas", "IG", "#d7473f"),
    JAGUAR("jaguar", "Jaguar", "J", "#ff7a59"),
    JOHNSON_AND_STARLEY("johnson-and-starley", "Johnson & Starley", "J&S", "#cf4b52"),
    KESTON("keston", "Keston", "K", "#7bb05d"),
    MAIN("main", "Main", "Main", "#e96a45", "/images/boiler-makes/main.svg"),
    MYSON("myson", "Myson", "M", "#456c89"),
    NAVIEN("navien", "Navien", "N", "#3b82f6"),
    POTTERTON("potterton", "Potterton", "P", "#455a7a", "/images/boiler-makes/potterton.svg"),
    RAVENHEAT("ravenheat", "Ravenheat", "RH", "#475569"),
    REMEHA("remeha", "Remeha", "R", "#2f3b52"),
    SAUNIER_DUVAL("saunier-duval", "Saunier Duval", "SD", "#ff6c5c"),
    VIESSMANN("viessmann", "Viessmann", "V", "#e16056"),
    VOKERA("vokera", "Vokera", "V", "#94a3b8", "/images/boiler-makes/vokera.svg"),
    WARMFLOW("warmflow", "Warmflow", "W", "#ec7b53"),
    ZANUSSI("zanussi", "Zanussi", "Z", "#1f2f57"),
    I_DO_NOT_KNOW("i-do-not-know", "I don't know", "?", "#94a3b8");

    private final String value;
    private final String label;
    private final String mark;
    private final String accentColor;
    private final String logoPath;

    BoilerMake(String value, String label, String mark, String accentColor) {
        this(value, label, mark, accentColor, null);
    }

    BoilerMake(String value, String label, String mark, String accentColor, String logoPath) {
        this.value = value;
        this.label = label;
        this.mark = mark;
        this.accentColor = accentColor;
        this.logoPath = logoPath;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getMark() {
        return mark;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getLogoPath() {
        return logoPath;
    }

    @JsonCreator
    public static BoilerMake fromValue(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Boiler make is null");
        }

        String normalized = input.trim().toLowerCase();

        for (BoilerMake make : values()) {
            if (make.value.equals(normalized)
                    || make.name().toLowerCase().equals(normalized.replace("-", "_"))) {
                return make;
            }
        }

        throw new IllegalArgumentException("Unsupported boiler make: " + input);
    }
}
