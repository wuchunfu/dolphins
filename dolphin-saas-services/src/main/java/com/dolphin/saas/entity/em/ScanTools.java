package com.dolphin.saas.entity.em;

public enum ScanTools {
    SONARSCAN("sonar"), VULSCAN("vuls");

    private final String info;

    ScanTools(String info) {
        this.info = info;
    }

    public String getName() {
        return info;
    }
}
