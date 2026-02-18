package com.blockymarketplace.model;

public class Product {
    private final String id;
    private final String serverId;
    private final String name;
    private final String description;
    private final int priceInCents;
    private final ProductType productType;
    private final String deliveryConfig;
    private final boolean active;

    public Product(String id, String serverId, String name, String description,
                   int priceInCents, ProductType productType, String deliveryConfig, boolean active) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
        this.description = description;
        this.priceInCents = priceInCents;
        this.productType = productType;
        this.deliveryConfig = deliveryConfig;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getServerId() {
        return serverId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    public String getFormattedPrice() {
        return String.format("$%.2f", priceInCents / 100.0);
    }

    public ProductType getProductType() {
        return productType;
    }

    public String getDeliveryConfig() {
        return deliveryConfig;
    }

    public boolean isActive() {
        return active;
    }
}
