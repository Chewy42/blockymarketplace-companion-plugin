package com.blockymarketplace.model;

public enum PurchaseStatus {
    PENDING("pending"),
    COMPLETED("completed"),
    FAILED("failed"),
    REFUNDED("refunded");

    private final String value;

    PurchaseStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PurchaseStatus fromString(String value) {
        for (PurchaseStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}
