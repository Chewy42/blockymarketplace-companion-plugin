package com.blockymarketplace.delivery;

import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RankDeliveryHandler implements DeliveryHandler {
    private static final Logger LOGGER = Logger.getLogger(RankDeliveryHandler.class.getName());

    @Override
    public CompletableFuture<DeliveryResult> deliver(UUID playerUuid, Purchase purchase, Product product) {
        String deliveryConfig = product.getDeliveryConfig();
        if (deliveryConfig == null || deliveryConfig.isBlank()) {
            LOGGER.log(Level.WARNING, "No delivery config for rank product: {0}", product.getId());
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid product configuration"));
        }

        try {
            JsonObject config = JsonParser.parseString(deliveryConfig).getAsJsonObject();
            String rankName = config.has("rankId")
                    ? config.get("rankId").getAsString()
                    : config.has("rank") ? config.get("rank").getAsString() : null;
            long durationMs = config.has("duration") ? config.get("duration").getAsLong() : -1;
            int durationDays = config.has("durationDays") ? config.get("durationDays").getAsInt() : -1;

            if (rankName == null || rankName.isBlank()) {
                return CompletableFuture.completedFuture(DeliveryResult.failure("Missing rank in config"));
            }

            String durationStr = durationMs > 0
                    ? String.format(" for %d ms", durationMs)
                    : durationDays > 0 ? String.format(" for %d days", durationDays) : " (permanent)";

            if (config.has("commands") || config.has("command")) {
                return CommandDeliveryHandlerUtils.executeConfiguredCommands(
                        config, playerUuid, purchase);
            }

            String command = durationMs > 0
                    ? String.format("perm group addtemp %s %s %d",
                    purchase.getPlayerName(), rankName, durationMs)
                    : durationDays > 0
                    ? String.format("perm group addtemp %s %s %d",
                    purchase.getPlayerName(), rankName, durationDays)
                    : String.format("perm group add %s %s", purchase.getPlayerName(), rankName);

            LOGGER.log(Level.INFO, "Granting rank {0}{1} to player {2}",
                    new Object[]{rankName, durationStr, playerUuid});

            if (!CommandExecution.executeAsConsole(command)) {
                return CompletableFuture.completedFuture(DeliveryResult.failure("Rank command failed"));
            }

            return CompletableFuture.completedFuture(
                    DeliveryResult.success(String.format("Granted rank %s%s", rankName, durationStr)));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse delivery config for product " + product.getId(), e);
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid delivery configuration"));
        }
    }
}
