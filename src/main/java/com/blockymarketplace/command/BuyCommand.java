package com.blockymarketplace.command;

import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.concurrent.CompletableFuture;

public class BuyCommand extends AbstractCommand {
    private final String webappUrl;

    public BuyCommand(LinkCodeService linkCodeService, ProductService productService,
                      PurchaseService purchaseService, String webappUrl) {
        super("buy", "Visit the marketplace to purchase items");
        this.webappUrl = webappUrl != null ? webappUrl : "https://blockymarketplace.com";
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }
        context.sendMessage(Message.raw("[Marketplace] Visit " + webappUrl + " to browse and purchase items."));
        context.sendMessage(Message.raw("[Marketplace] Items are delivered automatically when you're online."));
        return CompletableFuture.completedFuture(null);
    }
}
