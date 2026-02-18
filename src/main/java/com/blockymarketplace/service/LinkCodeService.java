package com.blockymarketplace.service;

import com.blockymarketplace.api.ConvexClient;
import com.blockymarketplace.model.LinkCode;
import com.blockymarketplace.model.LinkedAccount;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinkCodeService {
    private static final Logger LOGGER = Logger.getLogger(LinkCodeService.class.getName());
    private static final String CHARACTERS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConvexClient convexClient;
    private final String serverId;

    public LinkCodeService(ConvexClient convexClient, String serverId) {
        this.convexClient = convexClient;
        this.serverId = serverId;
    }

    public CompletableFuture<LinkCode> createLinkCode(UUID playerUuid, String playerName) {
        String code = generateCode();
        long expiresAt = Instant.now().plusSeconds(EXPIRY_MINUTES * 60).toEpochMilli();

        return convexClient.mutation("linkCodes:create", Map.of(
                "code", code,
                "playerUuid", playerUuid.toString(),
                "playerName", playerName,
                "serverId", serverId,
                "expiresAt", expiresAt
        )).thenApply(result -> {
            LOGGER.log(Level.INFO, "Created link code {0} for player {1}", new Object[]{code, playerName});
            return new LinkCode(code, playerUuid, playerName, serverId, Instant.ofEpochMilli(expiresAt));
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to create link code for player " + playerName, e);
            return null;
        });
    }

    public CompletableFuture<LinkedAccount> getLinkedAccount(UUID playerUuid) {
        return convexClient.query("linkedAccounts:getByPlayer", Map.of(
                "playerUuid", playerUuid.toString(),
                "serverId", serverId
        )).thenApply(result -> {
            if (result == null || result.isJsonNull()) {
                return null;
            }
            return parseLinkedAccount(result);
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to check linked account for player " + playerUuid, e);
            return null;
        });
    }

    public CompletableFuture<Boolean> isPlayerLinked(UUID playerUuid) {
        return getLinkedAccount(playerUuid).thenApply(account -> account != null);
    }

    public CompletableFuture<Boolean> unlinkAccount(UUID playerUuid) {
        return convexClient.mutation("linkedAccounts:unlinkByPlayer", Map.of(
                "playerUuid", playerUuid.toString(),
                "serverId", serverId
        )).thenApply(result -> {
            LOGGER.log(Level.INFO, "Unlinked account for player {0}", playerUuid);
            return true;
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to unlink account for player " + playerUuid, e);
            return false;
        });
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    private LinkedAccount parseLinkedAccount(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject obj = element.getAsJsonObject();
        return new LinkedAccount(
                obj.has("_id") ? obj.get("_id").getAsString() : null,
                UUID.fromString(obj.get("playerUuid").getAsString()),
                obj.get("clerkUserId").getAsString(),
                obj.get("serverId").getAsString(),
                Instant.ofEpochMilli(obj.get("linkedAt").getAsLong())
        );
    }
}
