package com.blockymarketplace.ui;

import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.model.PurchaseStatus;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class MarketplacePurchasesPage extends InteractiveCustomUIPage<MarketplaceEventData> {
    private static final int MAX_PURCHASES = 5;

    private final PurchaseService m_PurchaseService;
    private final ProductService m_ProductService;
    private final UUID m_PlayerUuid;
    private final PlayerRef m_PlayerRef;
    private List<Purchase> m_Purchases;
    private final Map<String, String> m_ProductNames;

    public MarketplacePurchasesPage(PlayerRef playerRef, PurchaseService purchaseService,
                                     ProductService productService) {
        super(playerRef, CustomPageLifetime.CanDismiss, MarketplaceEventData.CODEC);
        m_PlayerRef = playerRef;
        m_PurchaseService = purchaseService;
        m_ProductService = productService;
        m_PlayerUuid = playerRef.getUuid();
        m_Purchases = new ArrayList<>();
        m_ProductNames = new ConcurrentHashMap<>();
    }

    public void setPurchases(List<Purchase> purchases) {
        m_Purchases = purchases != null ? purchases : new ArrayList<>();
    }

    public void setProductName(String productId, String name) {
        if (productId != null && name != null) {
            m_ProductNames.put(productId, name);
        }
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder cmd,
                      UIEventBuilder events, Store<EntityStore> store) {
        cmd.append("pages/marketplace_purchases_page");

        populatePurchases(cmd);

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

    private void populatePurchases(UICommandBuilder cmd) {
        if (m_Purchases == null || m_Purchases.isEmpty()) {
            cmd.set("#noPurchasesLabel.opacity", "1");
            for (int i = 1; i <= MAX_PURCHASES; i++) {
                cmd.set("#purchase" + i + "Row.opacity", "0");
            }
            return;
        }

        cmd.set("#noPurchasesLabel.opacity", "0");

        for (int i = 1; i <= MAX_PURCHASES; i++) {
            if (i <= m_Purchases.size()) {
                Purchase purchase = m_Purchases.get(i - 1);
                String productName = m_ProductNames.getOrDefault(purchase.getProductId(), "Product");
                String statusText = getStatusText(purchase);
                String statusColor = getStatusColor(purchase);

                cmd.set("#purchase" + i + "Name.text", productName);
                cmd.set("#purchase" + i + "Status.text", statusText);
                cmd.set("#purchase" + i + "Status.style.color", statusColor);
                cmd.set("#purchase" + i + "Row.opacity", "1");
            } else {
                cmd.set("#purchase" + i + "Row.opacity", "0");
            }
        }
    }

    private String getStatusText(Purchase purchase) {
        if (purchase.isDelivered()) {
            return "Delivered";
        } else if (purchase.isCompleted()) {
            return "Completed";
        } else if (purchase.getStatus() == PurchaseStatus.FAILED) {
            return "Failed";
        } else {
            return "Pending";
        }
    }

    private String getStatusColor(Purchase purchase) {
        if (purchase.isDelivered()) {
            return "#00FF00";
        } else if (purchase.isCompleted()) {
            return "#4ECDC4";
        } else if (purchase.getStatus() == PurchaseStatus.FAILED) {
            return "#FF6B6B";
        } else {
            return "#FFD700";
        }
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
        }
    }

    private void handleRefresh(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        if (m_PurchaseService == null) {
            return;
        }

        m_PurchaseService.getPlayerPurchases(m_PlayerUuid)
            .thenAccept(purchases -> {
                m_Purchases = purchases;
                loadProductNames(purchases)
                    .thenRun(() -> {
                        player.getPageManager().openCustomPage(ref, store, this);
                    });
            });
    }

    private java.util.concurrent.CompletableFuture<Void> loadProductNames(List<Purchase> purchases) {
        if (m_ProductService == null || purchases == null || purchases.isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }

        List<java.util.concurrent.CompletableFuture<Void>> futures = new ArrayList<>();

        for (Purchase purchase : purchases) {
            String productId = purchase.getProductId();
            if (productId != null && !m_ProductNames.containsKey(productId)) {
                futures.add(
                    m_ProductService.getProductById(productId)
                        .thenAccept(product -> {
                            if (product != null) {
                                m_ProductNames.put(productId, product.getName());
                            }
                        })
                );
            }
        }

        return java.util.concurrent.CompletableFuture.allOf(
            futures.toArray(new java.util.concurrent.CompletableFuture[0])
        );
    }

    public List<Purchase> getPurchases() {
        return m_Purchases;
    }
}
