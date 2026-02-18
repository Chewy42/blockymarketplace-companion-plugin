package com.blockymarketplace.service;

import com.blockymarketplace.api.ConvexClient;
import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.model.PurchaseStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurchaseService {
    private static final Logger LOGGER = Logger.getLogger(PurchaseService.class.getName());

    private final ConvexClient convexClient;
    private final String serverId;

    public PurchaseService(ConvexClient convexClient, String serverId) {
        this.convexClient = convexClient;
        this.serverId = serverId;
    }

    public CompletableFuture<Purchase> createPurchase(UUID playerUuid, String playerName,
                                                       String productId, int amountInCents) {
        return convexClient.mutation("purchases:create", Map.of(
                "productId", productId,
                "serverId", serverId,
                "playerUuid", playerUuid.toString(),
                "playerName", playerName,
                "amountInCents", amountInCents
        )).thenApply(result -> {
            if (result == null || result.isJsonNull()) {
                return null;
            }
            JsonObject obj = result.getAsJsonObject();
            String id = obj.has("_id") ? obj.get("_id").getAsString() : null;
            LOGGER.log(Level.INFO, "Created purchase {0} for player {1}", new Object[]{id, playerName});
            return new Purchase(id, productId, serverId, playerUuid.toString(), playerName,
                    amountInCents, PurchaseStatus.PENDING, null, null, Instant.now());
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to create purchase for player " + playerName, e);
            return null;
        });
    }

    public CompletableFuture<List<Purchase>> getPlayerPurchases(UUID playerUuid) {
        return convexClient.query("purchases:getByPlayer", Map.of(
                "playerUuid", playerUuid.toString()
        )).thenApply(result -> {
            List<Purchase> purchases = new ArrayList<>();
            if (result != null && result.isJsonArray()) {
                JsonArray array = result.getAsJsonArray();
                for (JsonElement element : array) {
                    Purchase purchase = parsePurchase(element.getAsJsonObject());
                    if (purchase != null) {
                        purchases.add(purchase);
                    }
                }
            }
            return purchases;
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to fetch purchases for player " + playerUuid, e);
            return new ArrayList<>();
        });
    }

    public CompletableFuture<List<Purchase>> getPendingDeliveries(UUID playerUuid) {
        return getPlayerPurchases(playerUuid).thenApply(purchases ->
                purchases.stream()
                        .filter(p -> p.isCompleted() && !p.isDelivered())
                        .toList()
        );
    }

    public CompletableFuture<List<Purchase>> getPendingDeliveries(String playerUuidString) {
        return getPendingDeliveries(UUID.fromString(playerUuidString));
    }

    public CompletableFuture<Purchase> getPurchaseById(String purchaseId) {
        return convexClient.query("purchases:getById", Map.of(
                "id", purchaseId
        )).thenApply(result -> {
            if (result == null || result.isJsonNull()) {
                return null;
            }
            return parsePurchase(result.getAsJsonObject());
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to get purchase " + purchaseId, e);
            return null;
        });
    }

    public CompletableFuture<Boolean> updateStatus(String purchaseId, PurchaseStatus status, String polarCheckoutId) {
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("id", purchaseId);
        args.put("status", status.getValue());
        if (polarCheckoutId != null) {
            args.put("polarCheckoutId", polarCheckoutId);
        }

        return convexClient.mutation("purchases:updateStatus", args)
                .thenApply(result -> {
                    LOGGER.log(Level.INFO, "Updated purchase {0} to status {1}", new Object[]{purchaseId, status});
                    return true;
                }).exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "Failed to update purchase " + purchaseId + " status", e);
                    return false;
                });
    }

    public CompletableFuture<Boolean> markDelivered(String purchaseId) {
        return convexClient.mutation("purchases:markDelivered", Map.of(
                "id", purchaseId
        )).thenApply(result -> {
            LOGGER.log(Level.INFO, "Marked purchase {0} as delivered", purchaseId);
            return true;
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to mark purchase " + purchaseId + " as delivered", e);
            return false;
        });
    }

    public CompletableFuture<String> getCheckoutUrl(String purchaseId, String productName) {
        return convexClient.query("purchases:getCheckoutUrl", Map.of(
                "purchaseId", purchaseId
        )).thenApply(result -> {
            if (result != null && result.isJsonObject()) {
                JsonObject obj = result.getAsJsonObject();
                if (obj.has("checkoutUrl")) {
                    return obj.get("checkoutUrl").getAsString();
                }
            }
            return null;
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to get checkout URL for purchase " + purchaseId, e);
            return null;
        });
    }

    private Purchase parsePurchase(JsonObject obj) {
        try {
            String id = obj.has("_id") ? obj.get("_id").getAsString() : null;
            String productId = obj.has("productId") ? obj.get("productId").getAsString() : null;
            String serverId = obj.has("serverId") ? obj.get("serverId").getAsString() : null;
            String playerUuid = obj.has("playerUuid") ? obj.get("playerUuid").getAsString() : null;
            String playerName = obj.has("playerName") ? obj.get("playerName").getAsString() : "Unknown";
            int amountInCents = obj.has("amountInCents") ? obj.get("amountInCents").getAsInt() : 0;
            String statusStr = obj.has("status") ? obj.get("status").getAsString() : "pending";
            PurchaseStatus status = PurchaseStatus.fromString(statusStr);
            String polarCheckoutId = obj.has("polarCheckoutId") && !obj.get("polarCheckoutId").isJsonNull()
                    ? obj.get("polarCheckoutId").getAsString() : null;
            Instant deliveredAt = obj.has("deliveredAt") && !obj.get("deliveredAt").isJsonNull()
                    ? Instant.ofEpochMilli(obj.get("deliveredAt").getAsLong()) : null;
            Instant createdAt = obj.has("createdAt")
                    ? Instant.ofEpochMilli(obj.get("createdAt").getAsLong()) : Instant.now();

            return new Purchase(id, productId, serverId, playerUuid, playerName, amountInCents,
                    status, polarCheckoutId, deliveredAt, createdAt);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse purchase", e);
            return null;
        }
    }
}
