package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.List;
import java.util.UUID;

/**
 * Compatibility stub for disabled in-game marketplace UI.
 * Stores state only for command flow + unit test compatibility.
 */
public final class MarketplaceMainPage {
    private final LinkCodeService linkCodeService;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final String webappUrl;
    private final UUID playerUuid;
    private final String playerName;

    private LinkedAccount linkedAccount;
    private List<Product> products;
    private List<Purchase> purchases;

    public MarketplaceMainPage(PlayerRef playerRef,
                               LinkCodeService linkCodeService,
                               ProductService productService,
                               PurchaseService purchaseService,
                               String webappUrl) {
        this.linkCodeService = linkCodeService;
        this.productService = productService;
        this.purchaseService = purchaseService;
        this.webappUrl = webappUrl != null ? webappUrl : "https://blockymarketplace.com";
        this.playerUuid = playerRef != null ? playerRef.getUuid() : null;
        this.playerName = playerRef != null ? playerRef.getUsername() : null;
    }

    public void setLinkedAccount(LinkedAccount linkedAccount) {
        this.linkedAccount = linkedAccount;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }

    public LinkCodeService getLinkCodeService() {
        return linkCodeService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public PurchaseService getPurchaseService() {
        return purchaseService;
    }

    public String getWebappUrl() {
        return webappUrl;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public LinkedAccount getLinkedAccount() {
        return linkedAccount;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }
}
