package com.example.schedule.domain;

public enum PaymentStatus {
    NORMAL("정상"),
    OVERDUE_INSTALLMENT("연체"),
    ACCELERATED("기한이익상실");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
