package com.blockymarketplace.command;

import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.blockymarketplace.ui.MarketplaceMainPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BuyCommand extends AbstractCommand {
    private final LinkCodeService linkCodeService;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final String webappUrl;

    public BuyCommand(LinkCodeService linkCodeService, ProductService productService,
                      PurchaseService purchaseService, String webappUrl) {
        super("buy", "Open the marketplace interface");
        this.linkCodeService = linkCodeService;
        this.productService = productService;
        this.purchaseService = purchaseService;
        this.webappUrl = webappUrl != null ? webappUrl : "https://marketplace.blockynetwork.com";
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Only players can use this command."));
            return CompletableFuture.completedFuture(null);
        }

        if (linkCodeService == null || productService == null || purchaseService == null) {
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
                    var linkedAccountFuture = linkCodeService.getLinkedAccount(playerUuid);
                    var productsFuture = productService.getActiveProducts();
                    var purchasesFuture = purchaseService.getPlayerPurchases(playerUuid);

                    var linkedAccount = linkedAccountFuture.join();
                    var products = productsFuture.join();
                    var purchases = purchasesFuture.join();

                    world.execute(() -> {
                        MarketplaceMainPage mainPage = new MarketplaceMainPage(
                            playerRef, linkCodeService, productService, purchaseService, webappUrl
                        );
                        mainPage.setLinkedAccount(linkedAccount);
                        mainPage.setProducts(products);
                        mainPage.setPurchases(purchases);

                        Player player = store.getComponent(ref, Player.getComponentType());
                        player.getPageManager().openCustomPage(ref, store, mainPage);
                    });
                } catch (Exception e) {
                    world.execute(() -> {
                        context.sendMessage(Message.raw("Failed to open marketplace. Please try again."));
                    });
                }
            });
        });

        return CompletableFuture.completedFuture(null);
    }
}
