package com.blockymarketplace.model;

import java.time.Instant;
import java.util.UUID;

public class LinkedAccount {
    private final String id;
    private final UUID playerUuid;
    private final String clerkUserId;
    private final String serverId;
    private final Instant linkedAt;

    public LinkedAccount(String id, UUID playerUuid, String clerkUserId, String serverId, Instant linkedAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.clerkUserId = clerkUserId;
        this.serverId = serverId;
        this.linkedAt = linkedAt;
    }

    public String getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getClerkUserId() {
        return clerkUserId;
    }

    public String getServerId() {
        return serverId;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }
}
