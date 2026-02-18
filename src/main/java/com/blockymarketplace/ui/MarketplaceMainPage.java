package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.UUID;

public class MarketplaceMainPage extends InteractiveCustomUIPage<MarketplaceEventData> {
    private final LinkCodeService m_LinkCodeService;
    private final ProductService m_ProductService;
    private final PurchaseService m_PurchaseService;
    private final String m_WebappUrl;
    private final UUID m_PlayerUuid;
    private final String m_PlayerName;
    private final PlayerRef m_PlayerRef;
    private LinkedAccount m_LinkedAccount;
    private List<Product> m_Products;
    private List<Purchase> m_Purchases;

    public MarketplaceMainPage(PlayerRef playerRef, LinkCodeService linkCodeService,
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
    }

    public void setLinkedAccount(LinkedAccount linkedAccount) {
        m_LinkedAccount = linkedAccount;
    }

    public void setProducts(List<Product> products) {
        m_Products = products;
    }

    public void setPurchases(List<Purchase> purchases) {
        m_Purchases = purchases;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder cmd,
                      UIEventBuilder events, Store<EntityStore> store) {
        cmd.append("pages/marketplace_main_page");

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#profileButton",
            new EventData()
                .append("Action", "navigate")
                .append("Target", "profile")
        );

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#shopButton",
            new EventData()
                .append("Action", "navigate")
                .append("Target", "shop")
        );

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#purchasesButton",
            new EventData()
                .append("Action", "navigate")
                .append("Target", "purchases")
        );

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#closeButton",
            new EventData().append("Action", "close")
        );
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
            case "close" -> player.getPageManager().setPage(ref, store, null);
            case "navigate" -> handleNavigation(ref, store, player, data.target);
        }
    }

    private void handleNavigation(Ref<EntityStore> ref, Store<EntityStore> store, Player player, String target) {
        if (target == null) {
            return;
        }

        switch (target) {
            case "profile" -> {
                MarketplaceProfilePage profilePage = new MarketplaceProfilePage(
                    m_PlayerRef, m_LinkCodeService, m_WebappUrl, m_PlayerName
                );
                profilePage.setLinkedAccount(m_LinkedAccount);
                player.getPageManager().openCustomPage(ref, store, profilePage);
            }
            case "shop" -> {
                MarketplaceShopPage shopPage = new MarketplaceShopPage(
                    m_PlayerRef, m_LinkCodeService, m_ProductService, m_PurchaseService, m_WebappUrl
                );
                shopPage.setProducts(m_Products);
                shopPage.setLinkedAccount(m_LinkedAccount);
                player.getPageManager().openCustomPage(ref, store, shopPage);
            }
            case "purchases" -> {
                MarketplacePurchasesPage purchasesPage = new MarketplacePurchasesPage(
                    m_PlayerRef, m_PurchaseService, m_ProductService
                );
                purchasesPage.setPurchases(m_Purchases);
                player.getPageManager().openCustomPage(ref, store, purchasesPage);
            }
        }
    }

    public LinkCodeService getLinkCodeService() {
        return m_LinkCodeService;
    }

    public ProductService getProductService() {
        return m_ProductService;
    }

    public PurchaseService getPurchaseService() {
        return m_PurchaseService;
    }

    public String getWebappUrl() {
        return m_WebappUrl;
    }

    public UUID getPlayerUuid() {
        return m_PlayerUuid;
    }

    public String getPlayerName() {
        return m_PlayerName;
    }

    public LinkedAccount getLinkedAccount() {
        return m_LinkedAccount;
    }

    public List<Product> getProducts() {
        return m_Products;
    }

    public List<Purchase> getPurchases() {
        return m_Purchases;
    }
}
