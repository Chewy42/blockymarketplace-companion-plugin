package com.blockymarketplace.command;

import com.blockymarketplace.model.Product;
import com.blockymarketplace.service.ProductService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.concurrent.CompletableFuture;

public class ShopCommand extends AbstractCommand {
    private final ProductService productService;

    public ShopCommand(ProductService productService) {
        super("shop", "Browse available products in the marketplace");
        this.productService = productService;
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        if (productService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> ref = context.senderAsPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        CompletableFuture.runAsync(() -> {
            try {
                var products = productService.getActiveProducts().join();

                world.execute(() -> {
                    if (products.isEmpty()) {
                        context.sendMessage(Message.raw("=== Marketplace ==="));
                        context.sendMessage(Message.raw("No products available at this time."));
                        return;
                    }

                    context.sendMessage(Message.raw("=== Marketplace ==="));
                    context.sendMessage(Message.raw(""));

                    int index = 1;
                    for (Product product : products) {
                        String typeTag = getTypeTag(product);
                        String line = String.format("[%d] %s %s - %s",
                                index,
                                typeTag,
                                product.getName(),
                                product.getFormattedPrice());
                        context.sendMessage(Message.raw(line));

                        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                            context.sendMessage(Message.raw("    " + product.getDescription()));
                        }

                        index++;
                    }

                    context.sendMessage(Message.raw(""));
                    context.sendMessage(Message.raw("Use /buy <number> to purchase an item."));
                });
            } catch (Exception e) {
                world.execute(() -> {
                    context.sendMessage(Message.raw("Failed to load products. Please try again."));
                });
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    private String getTypeTag(Product product) {
        return switch (product.getProductType()) {
            case ITEM -> "[ITEM]";
            case RANK -> "[RANK]";
            case CURRENCY -> "[CURRENCY]";
            case COMMAND -> "[SPECIAL]";
        };
    }
}
