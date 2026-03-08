package com.blockymarketplace.ui;

import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compatibility stub for disabled in-game marketplace purchases UI.
 */
public final class MarketplacePurchasesPage {
    private final PlayerRef playerRef;
    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final Map<String, String> productNames = new ConcurrentHashMap<>();

    private List<Purchase> purchases = new ArrayList<>();

    public MarketplacePurchasesPage(PlayerRef playerRef,
                                    PurchaseService purchaseService,
                                    ProductService productService) {
        this.playerRef = playerRef;
        this.purchaseService = purchaseService;
        this.productService = productService;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases != null ? purchases : new ArrayList<>();
    }

    public void setProductName(String productId, String name) {
        if (productId != null && name != null) {
            productNames.put(productId, name);
        }
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }
}
