package com.blockymarketplace.command;

import com.blockymarketplace.delivery.DeliveryHandler;
import com.blockymarketplace.service.DeliveryService;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedeemPlayerCommand extends AbstractCommand {
    private final DeliveryService deliveryService;
    private final RequiredArg<String> playerArg;

    public RedeemPlayerCommand(DeliveryService deliveryService) {
        super("redeemplayer", "Redeem pending purchases for a player");
        this.deliveryService = deliveryService;
        this.playerArg = withRequiredArg("player", "Player name or UUID", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (deliveryService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        String playerInput = context.get(playerArg);
        UUID playerUuid = resolvePlayerUuid(playerInput);
        if (playerUuid == null) {
            context.sendMessage(Message.raw("Player not found or invalid UUID."));
            return CompletableFuture.completedFuture(null);
        }

        context.sendMessage(Message.raw("Redeeming pending deliveries for " + playerInput + "..."));

        return deliveryService.deliverPendingForPlayer(playerUuid)
                .thenAccept(results -> {
                    if (results.isEmpty()) {
                        context.sendMessage(Message.raw("No pending purchases found."));
                        return;
                    }

                    int successCount = 0;
                    int failCount = 0;
                    for (DeliveryHandler.DeliveryResult result : results) {
                        if (result.success()) {
                            successCount++;
                        } else {
                            failCount++;
                        }
                    }

                    context.sendMessage(Message.raw("Delivered: " + successCount + " | Failed: " + failCount));
                })
                .exceptionally(e -> {
                    context.sendMessage(Message.raw("Failed to redeem purchases."));
                    return null;
                });
    }

    private UUID resolvePlayerUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {
            PlayerRef playerRef = Universe.get().getPlayerByUsername(input, NameMatching.EXACT);
            if (playerRef != null) {
                return playerRef.getUuid();
            }
            return null;
        }
    }
}
