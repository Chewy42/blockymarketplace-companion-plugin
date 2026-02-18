package com.blockymarketplace.model;

import java.time.Instant;
import java.util.UUID;

public class LinkCode {
    private final String code;
    private final UUID playerUuid;
    private final String playerName;
    private final String serverId;
    private final Instant expiresAt;

    public LinkCode(String code, UUID playerUuid, String playerName, String serverId, Instant expiresAt) {
        this.code = code;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.serverId = serverId;
        this.expiresAt = expiresAt;
    }

    public String getCode() {
        return code;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getServerId() {
        return serverId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
