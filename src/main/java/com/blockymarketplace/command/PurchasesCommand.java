package com.blockymarketplace.command;

import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PurchasesCommand extends AbstractCommand {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final int MAX_DISPLAY = 10;

    private final PurchaseService purchaseService;

    public PurchasesCommand(PurchaseService purchaseService) {
        super("purchases", "View your purchase history");
        this.purchaseService = purchaseService;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        if (purchaseService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> ref = context.senderAsPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            UUID playerUuid = playerRef.getUuid();

            CompletableFuture.runAsync(() -> {
                try {
                    var purchases = purchaseService.getPlayerPurchases(playerUuid).join();

                world.execute(() -> {
                    if (purchases.isEmpty()) {
                        context.sendMessage(Message.raw("=== Your Purchases ==="));
                        context.sendMessage(Message.raw("You have no purchases yet."));
                        context.sendMessage(Message.raw("Use /shop to browse available products."));
                        return;
                    }

                    context.sendMessage(Message.raw("=== Your Purchases ==="));
                    context.sendMessage(Message.raw(""));

                    List<Purchase> pending = purchases.stream()
                            .filter(Purchase::isPending)
                            .toList();

                    List<Purchase> awaitingDelivery = purchases.stream()
                            .filter(p -> p.isCompleted() && !p.isDelivered())
                            .toList();

                    List<Purchase> delivered = purchases.stream()
                            .filter(Purchase::isDelivered)
                            .limit(MAX_DISPLAY)
                            .toList();

                    if (!pending.isEmpty()) {
                        context.sendMessage(Message.raw("-- Pending Payment --"));
                        for (Purchase p : pending) {
                            String line = String.format("  %s - %s (awaiting payment)",
                                    p.getFormattedAmount(),
                                    DATE_FORMAT.format(p.getCreatedAt()));
                            context.sendMessage(Message.raw(line));
                        }
                        context.sendMessage(Message.raw(""));
                    }

                    if (!awaitingDelivery.isEmpty()) {
                        context.sendMessage(Message.raw("-- Ready for Delivery --"));
                        for (Purchase p : awaitingDelivery) {
                            String line = String.format("  %s - Paid! Use /redeem to claim",
                                    p.getFormattedAmount());
                            context.sendMessage(Message.raw(line));
                        }
                        context.sendMessage(Message.raw(""));
                    }

                    if (!delivered.isEmpty()) {
                        context.sendMessage(Message.raw("-- Delivered --"));
                        for (Purchase p : delivered) {
                            String line = String.format("  %s - Delivered %s",
                                    p.getFormattedAmount(),
                                    DATE_FORMAT.format(p.getDeliveredAt()));
                            context.sendMessage(Message.raw(line));
                        }
                    }

                    if (purchases.size() > MAX_DISPLAY) {
                        context.sendMessage(Message.raw(""));
                        context.sendMessage(Message.raw("Showing " + MAX_DISPLAY + " of " + purchases.size() + " purchases."));
                    }
                });
            } catch (Exception e) {
                world.execute(() -> {
                    context.sendMessage(Message.raw("Failed to load purchases. Please try again."));
                });
            }
            });
        });

        return CompletableFuture.completedFuture(null);
    }
}
