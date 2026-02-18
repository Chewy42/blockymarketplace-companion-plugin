package com.blockymarketplace.service;

import com.blockymarketplace.api.ConvexClient;
import com.blockymarketplace.delivery.*;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.ProductType;
import com.blockymarketplace.model.Purchase;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeliveryService {
    private static final Logger LOGGER = Logger.getLogger(DeliveryService.class.getName());

    private final ConvexClient convexClient;
    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final Map<ProductType, DeliveryHandler> handlers;

    public DeliveryService(ConvexClient convexClient, PurchaseService purchaseService, ProductService productService) {
        this.convexClient = convexClient;
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.handlers = new EnumMap<>(ProductType.class);
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        handlers.put(ProductType.ITEM, new ItemDeliveryHandler());
        handlers.put(ProductType.RANK, new RankDeliveryHandler());
        handlers.put(ProductType.CURRENCY, new CurrencyDeliveryHandler());
        handlers.put(ProductType.COMMAND, new CommandDeliveryHandler());
    }

    public void registerHandler(ProductType type, DeliveryHandler handler) {
        handlers.put(type, handler);
    }

    public CompletableFuture<Boolean> deliverPurchase(String purchaseId) {
        return purchaseService.getPurchaseById(purchaseId)
                .thenCompose(purchase -> {
                    if (purchase == null) {
                        LOGGER.log(Level.WARNING, "Purchase not found for delivery: {0}", purchaseId);
                        return CompletableFuture.completedFuture(false);
                    }

                    return productService.getProductById(purchase.getProductId())
                            .thenCompose(product -> deliverWithHandler(purchase, product));
                });
    }

    public CompletableFuture<DeliveryHandler.DeliveryResult> deliverPurchaseToPlayer(
            UUID playerUuid, Purchase purchase, Product product) {
        if (product == null) {
            return CompletableFuture.completedFuture(
                    DeliveryHandler.DeliveryResult.failure("Product not found"));
        }

        DeliveryHandler handler = handlers.get(product.getProductType());
        if (handler == null) {
            LOGGER.log(Level.WARNING, "No handler for product type: {0}", product.getProductType());
            return CompletableFuture.completedFuture(
                    DeliveryHandler.DeliveryResult.failure("Unsupported product type"));
        }

        return handler.deliver(playerUuid, purchase, product)
                .thenCompose(result -> {
                    if (result.success()) {
                        return purchaseService.markDelivered(purchase.getId())
                                .thenApply(marked -> result);
                    }
                    return CompletableFuture.completedFuture(result);
                });
    }

    private CompletableFuture<Boolean> deliverWithHandler(Purchase purchase, Product product) {
        if (product == null) {
            LOGGER.log(Level.WARNING, "Product not found for purchase: {0}", purchase.getId());
            return CompletableFuture.completedFuture(false);
        }

        UUID playerUuid = UUID.fromString(purchase.getPlayerUuid());
        return deliverPurchaseToPlayer(playerUuid, purchase, product)
                .thenApply(DeliveryHandler.DeliveryResult::success);
    }

    public CompletableFuture<List<DeliveryHandler.DeliveryResult>> deliverPendingForPlayer(UUID playerUuid) {
        return purchaseService.getPendingDeliveries(playerUuid)
                .thenCompose(purchases -> {
                    if (purchases.isEmpty()) {
                        return CompletableFuture.completedFuture(List.of());
                    }

                    @SuppressWarnings("unchecked")
                    CompletableFuture<DeliveryHandler.DeliveryResult>[] futures = purchases.stream()
                            .map(purchase -> productService.getProductById(purchase.getProductId())
                                    .thenCompose(product -> deliverPurchaseToPlayer(playerUuid, purchase, product)))
                            .toArray(CompletableFuture[]::new);

                    return CompletableFuture.allOf(futures)
                            .thenApply(ignored -> {
                                List<DeliveryHandler.DeliveryResult> results = new java.util.ArrayList<>();
                                for (CompletableFuture<DeliveryHandler.DeliveryResult> f : futures) {
                                    results.add(f.join());
                                }
                                return results;
                            });
                });
    }

    public CompletableFuture<Void> deliverPendingForPlayer(String playerUuidString) {
        return deliverPendingForPlayer(UUID.fromString(playerUuidString))
                .thenApply(results -> null);
    }
}
