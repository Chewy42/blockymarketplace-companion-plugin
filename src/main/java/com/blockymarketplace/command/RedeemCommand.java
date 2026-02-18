package com.blockymarketplace.command;

import com.blockymarketplace.delivery.DeliveryHandler;
import com.blockymarketplace.service.DeliveryService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedeemCommand extends AbstractCommand {
    private final DeliveryService deliveryService;

    public RedeemCommand(DeliveryService deliveryService) {
        super("redeem", "Redeem pending purchases and deliveries");
        this.deliveryService = deliveryService;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        if (deliveryService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> ref = context.senderAsPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            UUID playerUuid = playerRef.getUuid();

            context.sendMessage(Message.raw("Checking for pending deliveries..."));

            deliveryService.deliverPendingForPlayer(playerUuid)
                    .thenAccept(results -> {
                        world.execute(() -> {
                            if (results.isEmpty()) {
                                context.sendMessage(Message.raw("You have no pending purchases to redeem."));
                                return;
                            }

                            int successCount = 0;
                            int failCount = 0;

                            context.sendMessage(Message.raw("=== Redeeming Purchases ==="));
                            for (DeliveryHandler.DeliveryResult result : results) {
                                if (result.success()) {
                                    successCount++;
                                    context.sendMessage(Message.raw("[OK] " + result.message()));
                                } else {
                                    failCount++;
                                    context.sendMessage(Message.raw("[FAIL] " + result.message()));
                                }
                            }

                            context.sendMessage(Message.raw(""));
                            context.sendMessage(Message.raw(String.format("Delivered: %d | Failed: %d", successCount, failCount)));

                            if (failCount > 0) {
                                context.sendMessage(Message.raw("Some items could not be delivered. Try again later or contact support."));
                            }
                        });
                    })
                    .exceptionally(e -> {
                        world.execute(() -> {
                            context.sendMessage(Message.raw("Failed to redeem purchases. Please try again."));
                        });
                        return null;
                    });
        });

        return CompletableFuture.completedFuture(null);
    }
}
