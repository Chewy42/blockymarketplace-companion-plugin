package com.blockymarketplace.model;

public enum ProductType {
    ITEM("item"),
    RANK("rank"),
    CURRENCY("currency"),
    COMMAND("command");

    private final String value;

    ProductType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProductType fromString(String value) {
        for (ProductType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return ITEM;
    }
}
