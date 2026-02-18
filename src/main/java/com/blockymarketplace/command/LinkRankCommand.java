package com.blockymarketplace.command;

import com.blockymarketplace.model.ProductType;
import com.blockymarketplace.service.ProductService;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;

import java.util.concurrent.CompletableFuture;

public class LinkRankCommand extends AbstractCommand {
    private final ProductService productService;
    private final RequiredArg<String> productIdArg;
    private final RequiredArg<String> rankIdArg;
    private final OptionalArg<Integer> durationDaysArg;

    public LinkRankCommand(ProductService productService) {
        super("linkrank", "Link a rank to a marketplace product");
        this.productService = productService;
        this.productIdArg = withRequiredArg("productId", "Product ID from the webapp", ArgTypes.STRING);
        this.rankIdArg = withRequiredArg("rankId", "Rank identifier to grant", ArgTypes.STRING);
        this.durationDaysArg = withOptionalArg("durationDays", "Optional duration in days", ArgTypes.INTEGER);
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (productService == null) {
            context.sendMessage(Message.raw("Marketplace is not configured. Please contact the server administrator."));
            return CompletableFuture.completedFuture(null);
        }

        String productId = context.get(productIdArg);
        if (productId == null || productId.isBlank()) {
            context.sendMessage(Message.raw("Product ID is required."));
            return CompletableFuture.completedFuture(null);
        }

        String rankId = context.get(rankIdArg);
        if (rankId == null || rankId.isBlank()) {
            context.sendMessage(Message.raw("Rank ID is required."));
            return CompletableFuture.completedFuture(null);
        }

        Integer durationDays = context.get(durationDaysArg);
        if (durationDays != null && durationDays <= 0) {
            durationDays = null;
        }

        JsonObject config = new JsonObject();
        config.addProperty("rankId", rankId);
        if (durationDays != null) {
            config.addProperty("durationDays", durationDays);
        }

        String message = durationDays != null
                ? "Linking rank " + rankId + " (" + durationDays + " days) to product " + productId + "..."
                : "Linking rank " + rankId + " to product " + productId + "...";
        context.sendMessage(Message.raw(message));

        return productService.updateDeliveryConfig(productId, config.toString(), ProductType.RANK)
                .thenAccept(success -> {
                    if (success) {
                        context.sendMessage(Message.raw("Linked rank " + rankId + " to product " + productId + "."));
                    } else {
                        context.sendMessage(Message.raw("Failed to link rank to product. Check the product ID."));
                    }
                })
                .exceptionally(e -> {
                    context.sendMessage(Message.raw("Failed to link rank to product."));
                    return null;
                });
    }
}
