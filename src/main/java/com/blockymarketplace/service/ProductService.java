package com.blockymarketplace.service;

import com.blockymarketplace.api.ConvexClient;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.ProductType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductService {
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());
    private static final long CACHE_TTL_MS = 60_000;

    private final ConvexClient convexClient;
    private final String serverId;
    private final String serverApiKey;
    private final Map<String, Product> productCache = new ConcurrentHashMap<>();
    private volatile long lastCacheRefresh = 0;

    public ProductService(ConvexClient convexClient, String serverId, String serverApiKey) {
        this.convexClient = convexClient;
        this.serverId = serverId;
        this.serverApiKey = serverApiKey;
    }

    public CompletableFuture<List<Product>> getActiveProducts() {
        if (isCacheValid()) {
            return CompletableFuture.completedFuture(new ArrayList<>(productCache.values()));
        }

        return refreshCache().thenApply(ignored -> new ArrayList<>(productCache.values()));
    }

    public CompletableFuture<List<Product>> getProductsByType(ProductType type) {
        return getActiveProducts().thenApply(products ->
                products.stream()
                        .filter(p -> p.getProductType() == type)
                        .toList()
        );
    }

    public CompletableFuture<Product> getProductById(String productId) {
        Product cached = productCache.get(productId);
        if (cached != null && isCacheValid()) {
            return CompletableFuture.completedFuture(cached);
        }

        return convexClient.query("products:getById", Map.of("id", productId))
                .thenApply(result -> {
                    if (result == null || result.isJsonNull()) {
                        return null;
                    }
                    Product product = parseProduct(result.getAsJsonObject());
                    if (product != null) {
                        productCache.put(product.getId(), product);
                    }
                    return product;
                })
                .exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "Failed to fetch product " + productId, e);
                    return productCache.get(productId);
                });
    }

    public CompletableFuture<Void> refreshCache() {
        return convexClient.query("products:getActiveByServer", Map.of("serverId", serverId))
                .thenAccept(result -> {
                    productCache.clear();
                    if (result != null && result.isJsonArray()) {
                        JsonArray products = result.getAsJsonArray();
                        for (JsonElement element : products) {
                            Product product = parseProduct(element.getAsJsonObject());
                            if (product != null) {
                                productCache.put(product.getId(), product);
                            }
                        }
                    }
                    lastCacheRefresh = System.currentTimeMillis();
                    LOGGER.log(Level.INFO, "Product cache refreshed: {0} products", productCache.size());
                })
                .exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "Failed to refresh product cache", e);
                    return null;
                });
    }

    public CompletableFuture<Boolean> updateDeliveryConfig(
            String productId,
            String deliveryConfig,
            ProductType expectedType
    ) {
        if (productId == null || productId.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        if (deliveryConfig == null || deliveryConfig.isBlank()) {
            return CompletableFuture.completedFuture(false);
        }
        if (serverApiKey == null || serverApiKey.isBlank()) {
            LOGGER.log(Level.WARNING, "Server API key missing; cannot update delivery config.");
            return CompletableFuture.completedFuture(false);
        }

        Map<String, Object> args = new HashMap<>();
        args.put("apiKey", serverApiKey);
        args.put("productId", productId);
        args.put("deliveryConfig", deliveryConfig);
        if (expectedType != null) {
            args.put("expectedType", expectedType.getValue());
        }

        return convexClient.mutation("products:updateDeliveryConfigFromServer", args)
                .thenApply(result -> {
                    Product cached = productCache.get(productId);
                    if (cached != null) {
                        productCache.put(productId, new Product(
                                cached.getId(),
                                cached.getServerId(),
                                cached.getName(),
                                cached.getDescription(),
                                cached.getPriceInCents(),
                                cached.getProductType(),
                                deliveryConfig,
                                cached.isActive()
                        ));
                    }
                    return true;
                })
                .exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "Failed to update delivery config for product " + productId, e);
                    return false;
                });
    }

    private boolean isCacheValid() {
        return System.currentTimeMillis() - lastCacheRefresh < CACHE_TTL_MS && !productCache.isEmpty();
    }

    private Product parseProduct(JsonObject obj) {
        try {
            String id = obj.has("_id") ? obj.get("_id").getAsString() : null;
            String serverId = obj.has("serverId") ? obj.get("serverId").getAsString() : null;
            String name = obj.has("name") ? obj.get("name").getAsString() : "Unknown";
            String description = obj.has("description") && !obj.get("description").isJsonNull()
                    ? obj.get("description").getAsString() : null;
            int priceInCents = obj.has("priceInCents") ? obj.get("priceInCents").getAsInt() : 0;
            String typeStr = obj.has("productType") ? obj.get("productType").getAsString() : "item";
            ProductType productType = ProductType.fromString(typeStr);
            String deliveryConfig = obj.has("deliveryConfig") && !obj.get("deliveryConfig").isJsonNull()
                    ? obj.get("deliveryConfig").getAsString() : null;
            String status = obj.has("status") && !obj.get("status").isJsonNull()
                    ? obj.get("status").getAsString() : null;
            boolean active = status != null ? "active".equalsIgnoreCase(status)
                    : obj.has("isActive") && obj.get("isActive").getAsBoolean();

            return new Product(id, serverId, name, description, priceInCents, productType, deliveryConfig, active);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse product", e);
            return null;
        }
    }
}
