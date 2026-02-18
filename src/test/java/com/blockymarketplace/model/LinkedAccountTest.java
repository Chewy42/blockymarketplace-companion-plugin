package com.blockymarketplace.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkedAccountTest {

    @Test
    void constructor_setsAllFields() {
        UUID playerUuid = UUID.randomUUID();
        Instant linkedAt = Instant.now();

        LinkedAccount account = new LinkedAccount("id123", playerUuid, "clerk_user_1", "server1", linkedAt);

        assertEquals("id123", account.getId());
        assertEquals(playerUuid, account.getPlayerUuid());
        assertEquals("clerk_user_1", account.getClerkUserId());
        assertEquals("server1", account.getServerId());
        assertEquals(linkedAt, account.getLinkedAt());
    }

    @Test
    void getters_returnCorrectValues() {
        UUID playerUuid = UUID.fromString("12345678-1234-1234-1234-123456789012");
        Instant linkedAt = Instant.parse("2024-01-15T10:30:00Z");

        LinkedAccount account = new LinkedAccount("convex_id", playerUuid, "user_abc", "my-server", linkedAt);

        assertEquals("convex_id", account.getId());
        assertEquals(playerUuid, account.getPlayerUuid());
        assertEquals("user_abc", account.getClerkUserId());
        assertEquals("my-server", account.getServerId());
        assertEquals(linkedAt, account.getLinkedAt());
    }
}
