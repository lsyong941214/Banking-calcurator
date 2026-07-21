package com.example.engine.domain;

public enum RepaymentType {
    EQUAL_PRINCIPAL_AND_INTEREST("원리금균등상환"),
    EQUAL_PRINCIPAL("원금균등상환"),
    BULK("만기일시상환");

    private final String label;

    RepaymentType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
