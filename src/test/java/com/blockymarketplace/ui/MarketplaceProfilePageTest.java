package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkCode;
import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.service.LinkCodeService;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketplaceProfilePageTest {

    @Mock
    private PlayerRef mockPlayerRef;

    @Mock
    private LinkCodeService mockLinkCodeService;

    private static final UUID TEST_UUID = UUID.randomUUID();
    private static final String TEST_NAME = "TestPlayer";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockPlayerRef.getUuid()).thenReturn(TEST_UUID);
        when(mockPlayerRef.getUsername()).thenReturn(TEST_NAME);
    }

    @Test
    void testConstructor() {
        MarketplaceProfilePage page = new MarketplaceProfilePage(
            mockPlayerRef, mockLinkCodeService, null, TEST_NAME
        );

        assertNull(page.getLinkedAccount());
        assertNull(page.getLinkCode());
    }

    @Test
    void testConstructorWithCustomWebappUrl() {
        String customUrl = "https://custom.example.com";
        MarketplaceProfilePage page = new MarketplaceProfilePage(
            mockPlayerRef, mockLinkCodeService, customUrl, TEST_NAME
        );

        assertNotNull(page);
    }

    @Test
    void testSetLinkedAccount() {
        MarketplaceProfilePage page = new MarketplaceProfilePage(
            mockPlayerRef, mockLinkCodeService, null, TEST_NAME
        );

        LinkedAccount account = new LinkedAccount("id", TEST_UUID, "clerk-123", "server-1", Instant.now());
        page.setLinkedAccount(account);

        assertNotNull(page.getLinkedAccount());
        assertEquals("clerk-123", page.getLinkedAccount().getClerkUserId());
    }

    @Test
    void testSetLinkCode() {
        MarketplaceProfilePage page = new MarketplaceProfilePage(
            mockPlayerRef, mockLinkCodeService, null, TEST_NAME
        );

        LinkCode code = new LinkCode("ABC123", TEST_UUID, TEST_NAME, "server-1", Instant.now().plusSeconds(300));
        page.setLinkCode(code);

        assertNotNull(page.getLinkCode());
        assertEquals("ABC123", page.getLinkCode().getCode());
    }

    @Test
    void testLinkedAccountNull() {
        MarketplaceProfilePage page = new MarketplaceProfilePage(
            mockPlayerRef, mockLinkCodeService, null, TEST_NAME
        );

        page.setLinkedAccount(null);
        assertNull(page.getLinkedAccount());
    }
}
