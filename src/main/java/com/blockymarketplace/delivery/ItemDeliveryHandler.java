package com.blockymarketplace.delivery;

import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemDeliveryHandler implements DeliveryHandler {
    private static final Logger LOGGER = Logger.getLogger(ItemDeliveryHandler.class.getName());

    @Override
    public CompletableFuture<DeliveryResult> deliver(UUID playerUuid, Purchase purchase, Product product) {
        String deliveryConfig = product.getDeliveryConfig();
        if (deliveryConfig == null || deliveryConfig.isBlank()) {
            LOGGER.log(Level.WARNING, "No delivery config for item product: {0}", product.getId());
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid product configuration"));
        }

        try {
            JsonObject config = JsonParser.parseString(deliveryConfig).getAsJsonObject();
            String itemId = config.has("itemId") ? config.get("itemId").getAsString() : null;
            int quantity = config.has("quantity") ? config.get("quantity").getAsInt() : 1;

            if (itemId == null) {
                return CompletableFuture.completedFuture(DeliveryResult.failure("Missing itemId in config"));
            }

            PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
            if (playerRef == null) {
                return CompletableFuture.completedFuture(DeliveryResult.playerOffline());
            }

            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null) {
                return CompletableFuture.completedFuture(DeliveryResult.playerOffline());
            }

            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();

            CompletableFuture<DeliveryResult> future = new CompletableFuture<>();
            world.execute(() -> {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player == null) {
                    future.complete(DeliveryResult.playerOffline());
                    return;
                }

                Inventory inventory = player.getInventory();
                ItemStack itemStack = new ItemStack(itemId, quantity);
                inventory.getCombinedBackpackStorageHotbar().addItemStack(itemStack);

                LOGGER.log(Level.INFO, "Delivering item {0} x{1} to player {2}",
                        new Object[]{itemId, quantity, playerUuid});

                future.complete(DeliveryResult.success(
                        String.format("Delivered %d x %s", quantity, itemId)));
            });

            return future;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse delivery config for product " + product.getId(), e);
            return CompletableFuture.completedFuture(DeliveryResult.failure("Invalid delivery configuration"));
        }
    }
}
