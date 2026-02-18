package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkCode;
import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.service.LinkCodeService;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MarketplaceProfilePage extends InteractiveCustomUIPage<MarketplaceEventData> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault());

    private final LinkCodeService m_LinkCodeService;
    private final String m_WebappUrl;
    private final UUID m_PlayerUuid;
    private final String m_PlayerName;
    private final PlayerRef m_PlayerRef;
    private LinkedAccount m_LinkedAccount;
    private LinkCode m_LinkCode;

    public MarketplaceProfilePage(PlayerRef playerRef, LinkCodeService linkCodeService,
                                   String webappUrl, String playerName) {
        super(playerRef, CustomPageLifetime.CanDismiss, MarketplaceEventData.CODEC);
        m_PlayerRef = playerRef;
        m_LinkCodeService = linkCodeService;
        m_WebappUrl = webappUrl != null ? webappUrl : "https://marketplace.blockynetwork.com";
        m_PlayerUuid = playerRef.getUuid();
        m_PlayerName = playerName;
    }

    public void setLinkedAccount(LinkedAccount linkedAccount) {
        m_LinkedAccount = linkedAccount;
    }

    public void setLinkCode(LinkCode linkCode) {
        m_LinkCode = linkCode;
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder cmd,
                      UIEventBuilder events, Store<EntityStore> store) {
        cmd.append("pages/marketplace_profile_page");

        cmd.set("#playerName.text", m_PlayerName);

        if (m_LinkedAccount != null) {
            cmd.set("#linkStatus.text", "Linked");
            cmd.set("#linkStatus.style.color", "#00FF00");
            cmd.set("#linkedDate.text", DATE_FORMATTER.format(m_LinkedAccount.getLinkedAt()));
            cmd.set("#linkCodeSection.opacity", "0");
            cmd.set("#generateCodeButton.opacity", "0");
        } else {
            cmd.set("#linkStatus.text", "Not Linked");
            cmd.set("#linkStatus.style.color", "#FF6B6B");
            cmd.set("#linkedDate.text", "");
            cmd.set("#unlinkButton.opacity", "0");
            cmd.set("#linkInstructions.text", "Visit " + m_WebappUrl + "/link");

            if (m_LinkCode != null && !m_LinkCode.isExpired()) {
                cmd.set("#linkCodeValue.text", m_LinkCode.getCode());
            }
        }

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#generateCodeButton",
            new EventData().append("Action", "generateCode")
        );

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#unlinkButton",
            new EventData().append("Action", "unlink")
        );

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#backButton",
            new EventData().append("Action", "back")
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
            case "back" -> player.getPageManager().setPage(ref, store, null);
            case "generateCode" -> handleGenerateCode(ref, store, player);
            case "unlink" -> handleUnlink(ref, store, player);
        }
    }

    private void handleGenerateCode(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        if (m_LinkCodeService == null) {
            return;
        }

        m_LinkCodeService.createLinkCode(m_PlayerUuid, m_PlayerName)
            .thenAccept(code -> {
                m_LinkCode = code;
                if (code != null) {
                    player.getPageManager().openCustomPage(ref, store, this);
                }
            });
    }

    private void handleUnlink(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        if (m_LinkCodeService == null) {
            return;
        }

        m_LinkCodeService.unlinkAccount(m_PlayerUuid)
            .thenAccept(success -> {
                if (success) {
                    m_LinkedAccount = null;
                    player.getPageManager().openCustomPage(ref, store, this);
                }
            });
    }

    public LinkedAccount getLinkedAccount() {
        return m_LinkedAccount;
    }

    public LinkCode getLinkCode() {
        return m_LinkCode;
    }
}
