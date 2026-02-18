package com.blockymarketplace.model;

import java.time.Instant;

public class Purchase {
    private final String id;
    private final String productId;
    private final String serverId;
    private final String playerUuid;
    private final String playerName;
    private final int amountInCents;
    private final PurchaseStatus status;
    private final String polarCheckoutId;
    private final Instant deliveredAt;
    private final Instant createdAt;

    public Purchase(String id, String productId, String serverId, String playerUuid,
                    String playerName, int amountInCents, PurchaseStatus status,
                    String polarCheckoutId, Instant deliveredAt, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.serverId = serverId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.amountInCents = amountInCents;
        this.status = status;
        this.polarCheckoutId = polarCheckoutId;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getServerId() {
        return serverId;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getAmountInCents() {
        return amountInCents;
    }

    public String getFormattedAmount() {
        return String.format("$%.2f", amountInCents / 100.0);
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public String getPolarCheckoutId() {
        return polarCheckoutId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isDelivered() {
        return deliveredAt != null;
    }

    public boolean isPending() {
        return status == PurchaseStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == PurchaseStatus.COMPLETED;
    }
}
