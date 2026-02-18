package com.blockymarketplace.webhook;

import com.blockymarketplace.model.PurchaseStatus;
import com.blockymarketplace.service.DeliveryService;
import com.blockymarketplace.service.PurchaseService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolarWebhookHandler {
    private static final Logger LOGGER = Logger.getLogger(PolarWebhookHandler.class.getName());

    private final PurchaseService purchaseService;
    private final DeliveryService deliveryService;

    public PolarWebhookHandler(PurchaseService purchaseService, DeliveryService deliveryService) {
        this.purchaseService = purchaseService;
        this.deliveryService = deliveryService;
    }

    public CompletableFuture<WebhookResult> handle(String payload) {
        try {
            JsonObject event = JsonParser.parseString(payload).getAsJsonObject();
            String eventType = event.has("type") ? event.get("type").getAsString() : null;

            if (eventType == null) {
                return CompletableFuture.completedFuture(WebhookResult.error("Missing event type"));
            }

            return switch (eventType) {
                case "checkout.session.completed" -> handleCheckoutCompleted(event);
                case "checkout.session.expired" -> handleCheckoutExpired(event);
                case "order.refunded" -> handleOrderRefunded(event);
                default -> {
                    LOGGER.log(Level.INFO, "Ignoring unhandled event type: {0}", eventType);
                    yield CompletableFuture.completedFuture(WebhookResult.success("Event ignored"));
                }
            };
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse webhook payload", e);
            return CompletableFuture.completedFuture(WebhookResult.error("Invalid payload"));
        }
    }

    private CompletableFuture<WebhookResult> handleCheckoutCompleted(JsonObject event) {
        JsonObject data = event.getAsJsonObject("data");
        if (data == null) {
            return CompletableFuture.completedFuture(WebhookResult.error("Missing data object"));
        }

        String checkoutId = data.has("id") ? data.get("id").getAsString() : null;
        String purchaseId = extractPurchaseIdFromMetadata(data);

        if (purchaseId == null) {
            LOGGER.log(Level.WARNING, "Checkout completed but no purchase ID in metadata: {0}", checkoutId);
            return CompletableFuture.completedFuture(WebhookResult.error("Missing purchase ID"));
        }

        LOGGER.log(Level.INFO, "Processing checkout completion for purchase: {0}", purchaseId);

        return purchaseService.updateStatus(purchaseId, PurchaseStatus.COMPLETED, checkoutId)
                .thenCompose(success -> {
                    if (!success) {
                        return CompletableFuture.completedFuture(WebhookResult.error("Failed to update purchase"));
                    }

                    if (deliveryService != null) {
                        return deliveryService.deliverPurchase(purchaseId)
                                .thenApply(delivered -> {
                                    if (delivered) {
                                        return WebhookResult.success("Purchase completed and delivered");
                                    }
                                    return WebhookResult.success("Purchase completed, delivery pending");
                                });
                    }
                    return CompletableFuture.completedFuture(WebhookResult.success("Purchase completed"));
                });
    }

    private CompletableFuture<WebhookResult> handleCheckoutExpired(JsonObject event) {
        JsonObject data = event.getAsJsonObject("data");
        if (data == null) {
            return CompletableFuture.completedFuture(WebhookResult.error("Missing data object"));
        }

        String purchaseId = extractPurchaseIdFromMetadata(data);
        if (purchaseId == null) {
            return CompletableFuture.completedFuture(WebhookResult.success("No purchase to update"));
        }

        LOGGER.log(Level.INFO, "Checkout expired for purchase: {0}", purchaseId);

        return purchaseService.updateStatus(purchaseId, PurchaseStatus.FAILED, null)
                .thenApply(success -> success
                        ? WebhookResult.success("Purchase marked as failed")
                        : WebhookResult.error("Failed to update purchase"));
    }

    private CompletableFuture<WebhookResult> handleOrderRefunded(JsonObject event) {
        JsonObject data = event.getAsJsonObject("data");
        if (data == null) {
            return CompletableFuture.completedFuture(WebhookResult.error("Missing data object"));
        }

        String purchaseId = extractPurchaseIdFromMetadata(data);
        if (purchaseId == null) {
            return CompletableFuture.completedFuture(WebhookResult.success("No purchase to update"));
        }

        LOGGER.log(Level.INFO, "Order refunded for purchase: {0}", purchaseId);

        return purchaseService.updateStatus(purchaseId, PurchaseStatus.REFUNDED, null)
                .thenApply(success -> success
                        ? WebhookResult.success("Purchase marked as refunded")
                        : WebhookResult.error("Failed to update purchase"));
    }

    private String extractPurchaseIdFromMetadata(JsonObject data) {
        JsonElement metadata = data.get("metadata");
        if (metadata == null || metadata.isJsonNull()) {
            return null;
        }
        JsonObject metaObj = metadata.getAsJsonObject();
        return metaObj.has("purchaseId") ? metaObj.get("purchaseId").getAsString() : null;
    }

    public record WebhookResult(boolean success, String message) {
        public static WebhookResult success(String message) {
            return new WebhookResult(true, message);
        }

        public static WebhookResult error(String message) {
            return new WebhookResult(false, message);
        }
    }
}
