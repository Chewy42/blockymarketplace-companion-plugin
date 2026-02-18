package com.blockymarketplace.delivery;

import com.blockymarketplace.model.Purchase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CommandDeliveryHandlerUtils {
    private static final Logger LOGGER = Logger.getLogger(CommandDeliveryHandlerUtils.class.getName());

    private CommandDeliveryHandlerUtils() {}

    public static CompletableFuture<DeliveryHandler.DeliveryResult> executeConfiguredCommands(
            JsonObject config, UUID playerUuid, Purchase purchase) {
        List<String> commands = new ArrayList<>();
        if (config.has("commands") && config.get("commands").isJsonArray()) {
            JsonArray commandArray = config.getAsJsonArray("commands");
            for (int i = 0; i < commandArray.size(); i++) {
                String cmd = commandArray.get(i).getAsString();
                cmd = replacePlaceholders(cmd, playerUuid, purchase);
                commands.add(cmd);
            }
        } else if (config.has("command")) {
            String cmd = config.get("command").getAsString();
            cmd = replacePlaceholders(cmd, playerUuid, purchase);
            commands.add(cmd);
        }

        if (commands.isEmpty()) {
            return CompletableFuture.completedFuture(DeliveryHandler.DeliveryResult.failure("No commands configured"));
        }

        for (String command : commands) {
            LOGGER.log(Level.INFO, "Executing command for player {0}: {1}",
                    new Object[]{playerUuid, command});
            if (!CommandExecution.executeAsConsole(command)) {
                return CompletableFuture.completedFuture(
                        DeliveryHandler.DeliveryResult.failure("Command execution failed"));
            }
        }

        return CompletableFuture.completedFuture(
                DeliveryHandler.DeliveryResult.success(String.format("Executed %d command(s)", commands.size())));
    }

    private static String replacePlaceholders(String cmd, UUID playerUuid, Purchase purchase) {
        return cmd.replace("{player}", playerUuid.toString())
                .replace("{playerName}", purchase.getPlayerName())
                .replace("$username", purchase.getPlayerName());
    }
}
