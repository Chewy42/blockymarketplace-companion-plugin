package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MarketplaceShopPage extends InteractiveCustomUIPage<MarketplaceEventData> {
    private static final int MAX_PRODUCTS = 6;

    private final LinkCodeService m_LinkCodeService;
    private final ProductService m_ProductService;
    private final PurchaseService m_PurchaseService;
    private final String m_WebappUrl;
    private final UUID m_PlayerUuid;
    private final String m_PlayerName;
    private final PlayerRef m_PlayerRef;
    private List<Product> m_Products;
    private LinkedAccount m_LinkedAccount;

    public MarketplaceShopPage(PlayerRef playerRef, LinkCodeService linkCodeService,
                                ProductService productService, PurchaseService purchaseService,
                                String webappUrl) {
        super(playerRef, CustomPageLifetime.CanDismiss, MarketplaceEventData.CODEC);
        m_PlayerRef = playerRef;
        m_LinkCodeService = linkCodeService;
        m_ProductService = productService;
        m_PurchaseService = purchaseService;
        m_WebappUrl = webappUrl != null ? webappUrl : "https://marketplace.blockynetwork.com";
        m_PlayerUuid = playerRef.getUuid();
        m_PlayerName = playerRef.getUsername();
        m_Products = new ArrayList<>();
    }

    public void setProducts(List<Product> products) {
        m_Products = products != null ? products : new ArrayList<>();
    }

    public void setLinkedAccount(LinkedAccount linkedAccount) {
        m_LinkedAccount = linkedAccount;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder cmd,
                      UIEventBuilder events, Store<EntityStore> store) {
        cmd.append("pages/marketplace_shop_page");

        populateProducts(cmd);

        for (int i = 1; i <= MAX_PRODUCTS; i++) {
            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#product" + i + "Buy",
                new EventData()
                    .append("Action", "buy")
                    .append("Target", String.valueOf(i))
            );
        }

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#refreshButton",
            new EventData().append("Action", "refresh")
        );

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#backButton",
            new EventData().append("Action", "back")
        );
    }

    private void populateProducts(UICommandBuilder cmd) {
        if (m_Products == null || m_Products.isEmpty()) {
            cmd.set("#noProductsLabel.opacity", "1");
            for (int i = 1; i <= MAX_PRODUCTS; i++) {
                cmd.set("#product" + i + "Row.opacity", "0");
            }
            return;
        }

        cmd.set("#noProductsLabel.opacity", "0");

        for (int i = 1; i <= MAX_PRODUCTS; i++) {
            if (i <= m_Products.size()) {
                Product product = m_Products.get(i - 1);
                String typePrefix = getTypePrefix(product);
                cmd.set("#product" + i + "Name.text", typePrefix + " " + product.getName());
                cmd.set("#product" + i + "Price.text", product.getFormattedPrice());
                cmd.set("#product" + i + "Row.opacity", "1");
            } else {
                cmd.set("#product" + i + "Row.opacity", "0");
            }
        }
    }

    private String getTypePrefix(Product product) {
        return switch (product.getProductType()) {
            case ITEM -> "[ITEM]";
            case RANK -> "[RANK]";
            case CURRENCY -> "[CURRENCY]";
            case COMMAND -> "[SPECIAL]";
        };
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store,
                                MarketplaceEventData data) {
        if (data == null || data.action == null) {
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }

        switch (data.action) {
            case "back" -> player.getPageManager().setPage(ref, store, null);
            case "refresh" -> handleRefresh(ref, store, player);
            case "buy" -> handleBuy(ref, store, player, data.target);
        }
    }

    private void handleRefresh(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        if (m_ProductService == null) {
            return;
        }

        m_ProductService.refreshCache()
            .thenCompose(ignored -> m_ProductService.getActiveProducts())
            .thenAccept(products -> {
                m_Products = products;
                player.getPageManager().openCustomPage(ref, store, this);
            });
    }

    private void handleBuy(Ref<EntityStore> ref, Store<EntityStore> store, Player player, String indexStr) {
        if (indexStr == null || m_PurchaseService == null || m_LinkCodeService == null) {
            return;
        }

        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            return;
        }

        if (index < 1 || index > m_Products.size()) {
            return;
        }

        m_LinkCodeService.isPlayerLinked(m_PlayerUuid)
            .thenAccept(isLinked -> {
                if (!isLinked) {
                    player.sendMessage(Message.raw("You must link your account first. Go to Profile & Link Status."));
                    return;
                }

                Product product = m_Products.get(index - 1);
                m_PurchaseService.createPurchase(m_PlayerUuid, m_PlayerName, product.getId(), product.getPriceInCents())
                    .thenCompose(purchase -> {
                        if (purchase == null) {
                            player.sendMessage(Message.raw("Failed to create purchase. Please try again."));
                            return java.util.concurrent.CompletableFuture.completedFuture(null);
                        }
                        return m_PurchaseService.getCheckoutUrl(purchase.getId(), product.getName())
                            .thenApply(url -> {
                                player.sendMessage(Message.raw("Purchase created for: " + product.getName()));
                                player.sendMessage(Message.raw("Price: " + product.getFormattedPrice()));
                                if (url != null) {
                                    player.sendMessage(Message.raw("Complete payment at: " + url));
                                } else {
                                    player.sendMessage(Message.raw("Visit: " + m_WebappUrl + "/checkout/" + purchase.getId()));
                                }
                                return null;
                            });
                    });
            });
    }

    public List<Product> getProducts() {
        return m_Products;
    }

    public LinkedAccount getLinkedAccount() {
        return m_LinkedAccount;
    }
}
