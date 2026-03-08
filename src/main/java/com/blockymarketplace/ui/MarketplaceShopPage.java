package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility stub for disabled in-game marketplace shop UI.
 */
public final class MarketplaceShopPage {
    private final PlayerRef playerRef;
    private final LinkCodeService linkCodeService;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final String webappUrl;

    private List<Product> products = new ArrayList<>();
    private LinkedAccount linkedAccount;

    public MarketplaceShopPage(PlayerRef playerRef,
                               LinkCodeService linkCodeService,
                               ProductService productService,
                               PurchaseService purchaseService,
                               String webappUrl) {
        this.playerRef = playerRef;
        this.linkCodeService = linkCodeService;
        this.productService = productService;
        this.purchaseService = purchaseService;
        this.webappUrl = webappUrl != null ? webappUrl : "https://blockymarketplace.com";
    }

    public void setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
    }

    public void setLinkedAccount(LinkedAccount linkedAccount) {
        this.linkedAccount = linkedAccount;
    }

    public List<Product> getProducts() {
        return products;
    }

    public LinkedAccount getLinkedAccount() {
        return linkedAccount;
    }
}
