package com.blockymarketplace.delivery;

import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CurrencyDeliveryHandler implements DeliveryHandler {
    private static final Logger LOGGER = Logger.getLogger(CurrencyDeliveryHandler.class.getName());

    @Override
    public CompletableFuture<DeliveryResult> deliver(UUID playerUuid, Purchase purchase, Product product) {
        String deliveryConfig = product.getDeliveryConfig();
        if (deliveryConfig == null || deliveryConfig.isBlank()) {
            LOGGER.log(Level.WARNING, "No delivery config for currency product: {0}", product.getId());
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid product configuration"));
        }

        try {
            JsonObject config = JsonParser.parseString(deliveryConfig).getAsJsonObject();
            String currencyType = config.has("currencyId")
                    ? config.get("currencyId").getAsString()
                    : config.has("currency") ? config.get("currency").getAsString() : "coins";
            int amount = config.has("amount") ? config.get("amount").getAsInt() : 0;

            if (amount <= 0) {
                return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid currency amount"));
            }

            if (config.has("commands") || config.has("command")) {
                return CommandDeliveryHandlerUtils.executeConfiguredCommands(
                        config, playerUuid, purchase);
            }

            String command = String.format("currency add %s %d %s",
                    purchase.getPlayerName(), amount, currencyType);

            LOGGER.log(Level.INFO, "Adding {0} {1} to player {2}",
                    new Object[]{amount, currencyType, playerUuid});

            if (!CommandExecution.executeAsConsole(command)) {
                return CompletableFuture.completedFuture(DeliveryResult.failure("Currency command failed"));
            }

            return CompletableFuture.completedFuture(
                    DeliveryResult.success(String.format("Added %d %s", amount, currencyType)));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse delivery config for product " + product.getId(), e);
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid delivery configuration"));
        }
    }
}
