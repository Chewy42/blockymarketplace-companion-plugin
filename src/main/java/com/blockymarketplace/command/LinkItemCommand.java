package com.blockymarketplace.command;

import com.blockymarketplace.model.ProductType;
import com.blockymarketplace.service.ProductService;
import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.concurrent.CompletableFuture;

public class LinkItemCommand extends AbstractCommand {
    private final ProductService productService;
    private final RequiredArg<String> productIdArg;

    public LinkItemCommand(ProductService productService) {
        super("linkitem", "Link the held item to a marketplace product");
        this.productService = productService;
        this.productIdArg = withRequiredArg("productId", "Product ID from the webapp", ArgTypes.STRING);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (productService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        String productId = context.get(productIdArg);
        if (productId == null || productId.isBlank()) {
            context.sendMessage(Message.raw("Product ID is required."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> ref = context.senderAsPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        world.execute(() -> {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                context.sendMessage(Message.raw("Unable to access player inventory."));
                return;
            }

            Inventory inventory = player.getInventory();
            ItemStack itemStack = inventory.getItemInHand();
            if (itemStack == null || itemStack.isEmpty()) {
                context.sendMessage(Message.raw("Hold an item in your hand to link it to the product."));
                return;
            }

            String itemId = itemStack.getItemId();
            int quantity = itemStack.getQuantity();

            JsonObject config = new JsonObject();
            config.addProperty("itemId", itemId);
            config.addProperty("quantity", quantity);

            context.sendMessage(Message.raw("Linking held item to product " + productId + "..."));

            productService.updateDeliveryConfig(productId, config.toString(), ProductType.ITEM)
                    .thenAccept(success -> world.execute(() -> {
                        if (success) {
                            context.sendMessage(Message.raw(
                                    "Linked " + quantity + " x " + itemId + " to product " + productId + "."));
                        } else {
                            context.sendMessage(Message.raw("Failed to link item to product. Check the product ID."));
                        }
                    }))
                    .exceptionally(e -> {
                        world.execute(() -> context.sendMessage(Message.raw("Failed to link item to product.")));
                        return null;
                    });
        });

        return CompletableFuture.completedFuture(null);
    }
}
