package com.blockymarketplace.listener;

import com.blockymarketplace.service.DeliveryService;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerEventListener {
    private static final Logger LOGGER = Logger.getLogger(PlayerEventListener.class.getName());

    private final DeliveryService deliveryService;

    public PlayerEventListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    public void onPlayerConnect(PlayerConnectEvent event) {
        UUID playerUuid = event.getPlayerRef().getUuid();
        LOGGER.log(Level.FINE, "Player connected: {0}, checking for pending deliveries", playerUuid);

        deliveryService.deliverPendingForPlayer(playerUuid)
                .thenAccept(results -> {
                    if (!results.isEmpty()) {
                        long successCount = results.stream()
                                .filter(r -> r.success())
                                .count();
                        LOGGER.log(Level.INFO, "Auto-delivered {0} items to player {1}",
                                new Object[]{successCount, playerUuid});
                    }
                })
                .exceptionally(e -> {
                    LOGGER.log(Level.WARNING, "Failed to auto-deliver to player " + playerUuid, e);
                    return null;
                });
    }
}
