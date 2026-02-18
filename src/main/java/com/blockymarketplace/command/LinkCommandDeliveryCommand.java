package com.blockymarketplace.command;

import com.blockymarketplace.model.ProductType;
import com.blockymarketplace.service.ProductService;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;

import java.util.concurrent.CompletableFuture;

public class LinkCommandDeliveryCommand extends AbstractCommand {
    private final ProductService productService;
    private final RequiredArg<String> productIdArg;
    private final RequiredArg<String> commandArg;

    public LinkCommandDeliveryCommand(ProductService productService) {
        super("linkcommand", "Link a command delivery to a marketplace product");
        this.productService = productService;
        this.productIdArg = withRequiredArg("productId", "Product ID from the webapp", ArgTypes.STRING);
        this.commandArg = withRequiredArg("command", "Command to run on purchase", ArgTypes.STRING);
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

        String rawCommand = context.get(commandArg);
        String normalizedCommand = normalizeCommand(rawCommand);
        if (normalizedCommand.isBlank()) {
            context.sendMessage(Message.raw("Command is required."));
            return CompletableFuture.completedFuture(null);
        }

        JsonObject config = new JsonObject();
        config.addProperty("command", normalizedCommand);

        context.sendMessage(Message.raw("Linking command delivery to product " + productId + "..."));

        return productService.updateDeliveryConfig(productId, config.toString(), ProductType.COMMAND)
                .thenAccept(success -> {
                    if (success) {
                        context.sendMessage(Message.raw("Linked command delivery to product " + productId + "."));
                    } else {
                        context.sendMessage(Message.raw("Failed to link command delivery. Check the product ID."));
                    }
                })
                .exceptionally(e -> {
                    context.sendMessage(Message.raw("Failed to link command delivery."));
                    return null;
                });
    }

    private String normalizeCommand(String command) {
        if (command == null) {
            return "";
        }
        return command.trim();
    }
}
