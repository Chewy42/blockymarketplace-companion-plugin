package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkCode;
import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.service.LinkCodeService;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Compatibility stub for disabled in-game marketplace profile UI.
 */
public final class MarketplaceProfilePage {
    private final PlayerRef playerRef;
    private final LinkCodeService linkCodeService;
    private final String webappUrl;
    private final String playerName;

    private LinkedAccount linkedAccount;
    private LinkCode linkCode;

    public MarketplaceProfilePage(PlayerRef playerRef,
                                  LinkCodeService linkCodeService,
                                  String webappUrl,
                                  String playerName) {
        this.playerRef = playerRef;
        this.linkCodeService = linkCodeService;
        this.webappUrl = webappUrl != null ? webappUrl : "https://blockymarketplace.com";
        this.playerName = playerName;
    }

    public void setLinkedAccount(LinkedAccount linkedAccount) {
        this.linkedAccount = linkedAccount;
    }

    public void setLinkCode(LinkCode linkCode) {
        this.linkCode = linkCode;
    }

    public LinkedAccount getLinkedAccount() {
        return linkedAccount;
    }

    public LinkCode getLinkCode() {
        return linkCode;
    }
}
