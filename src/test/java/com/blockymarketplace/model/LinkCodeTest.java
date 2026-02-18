package com.blockymarketplace.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkCodeTest {

    @Test
    void constructor_setsAllFields() {
        UUID playerUuid = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(300);

        LinkCode linkCode = new LinkCode("ABC123", playerUuid, "TestPlayer", "server1", expiresAt);

        assertEquals("ABC123", linkCode.getCode());
        assertEquals(playerUuid, linkCode.getPlayerUuid());
        assertEquals("TestPlayer", linkCode.getPlayerName());
        assertEquals("server1", linkCode.getServerId());
        assertEquals(expiresAt, linkCode.getExpiresAt());
    }

    @Test
    void isExpired_returnsFalseForFutureExpiry() {
        Instant futureExpiry = Instant.now().plusSeconds(300);
        LinkCode linkCode = new LinkCode("ABC123", UUID.randomUUID(), "TestPlayer", "server1", futureExpiry);

        assertFalse(linkCode.isExpired());
    }

    @Test
    void isExpired_returnsTrueForPastExpiry() {
        Instant pastExpiry = Instant.now().minusSeconds(10);
        LinkCode linkCode = new LinkCode("ABC123", UUID.randomUUID(), "TestPlayer", "server1", pastExpiry);

        assertTrue(linkCode.isExpired());
    }
}
