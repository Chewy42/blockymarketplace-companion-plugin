package com.blockymarketplace.delivery;

import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DeliveryHandler {
    CompletableFuture<DeliveryResult> deliver(UUID playerUuid, Purchase purchase, Product product);

    record DeliveryResult(boolean success, String message) {
        public static DeliveryResult success(String message) {
            return new DeliveryResult(true, message);
        }

        public static DeliveryResult failure(String message) {
            return new DeliveryResult(false, message);
        }

        public static DeliveryResult playerOffline() {
            return new DeliveryResult(false, "Player is offline");
        }
    }
}
