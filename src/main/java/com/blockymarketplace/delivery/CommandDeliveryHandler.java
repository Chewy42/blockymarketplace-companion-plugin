package com.blockymarketplace.delivery;

import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandDeliveryHandler implements DeliveryHandler {
    private static final Logger LOGGER = Logger.getLogger(CommandDeliveryHandler.class.getName());

    @Override
    public CompletableFuture<DeliveryResult> deliver(UUID playerUuid, Purchase purchase, Product product) {
        String deliveryConfig = product.getDeliveryConfig();
        if (deliveryConfig == null || deliveryConfig.isBlank()) {
            LOGGER.log(Level.WARNING, "No delivery config for command product: {0}", product.getId());
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid product configuration"));
        }

        try {
            JsonObject config = JsonParser.parseString(deliveryConfig).getAsJsonObject();
            List<String> commands = new ArrayList<>();

            if (config.has("commands") && config.get("commands").isJsonArray()) {
                JsonArray commandArray = config.getAsJsonArray("commands");
                for (int i = 0; i < commandArray.size(); i++) {
                    String cmd = commandArray.get(i).getAsString();
                    cmd = cmd.replace("{player}", playerUuid.toString())
                             .replace("{playerName}", purchase.getPlayerName())
                             .replace("$username", purchase.getPlayerName());
                    commands.add(cmd);
                }
            } else if (config.has("command")) {
                String cmd = config.get("command").getAsString();
                cmd = cmd.replace("{player}", playerUuid.toString())
                         .replace("{playerName}", purchase.getPlayerName())
                         .replace("$username", purchase.getPlayerName());
                commands.add(cmd);
            }

            if (commands.isEmpty()) {
                return CompletableFuture.completedFuture(DeliveryResult.failure("No commands configured"));
            }

            for (String command : commands) {
                LOGGER.log(Level.INFO, "Executing command for player {0}: {1}",
                        new Object[]{playerUuid, command});
                if (!CommandExecution.executeAsConsole(command)) {
                    return CompletableFuture.completedFuture(
                            DeliveryResult.failure("Command execution failed"));
                }
            }

            return CompletableFuture.completedFuture(
                    DeliveryResult.success(String.format("Executed %d command(s)", commands.size())));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse delivery config for product " + product.getId(), e);
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid delivery configuration"));
        }
    }
}
