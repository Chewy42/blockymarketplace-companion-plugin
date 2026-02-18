package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.ProductType;
import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.model.PurchaseStatus;
import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketplaceMainPageTest {

    @Mock
    private PlayerRef mockPlayerRef;

    @Mock
    private LinkCodeService mockLinkCodeService;

    @Mock
    private ProductService mockProductService;

    @Mock
    private PurchaseService mockPurchaseService;

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
        MarketplaceMainPage page = new MarketplaceMainPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertEquals(TEST_UUID, page.getPlayerUuid());
        assertEquals(TEST_NAME, page.getPlayerName());
        assertNotNull(page.getWebappUrl());
    }

    @Test
    void testConstructorWithCustomWebappUrl() {
        String customUrl = "https://custom.example.com";
        MarketplaceMainPage page = new MarketplaceMainPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, customUrl
        );

        assertEquals(customUrl, page.getWebappUrl());
    }

    @Test
    void testSetLinkedAccount() {
        MarketplaceMainPage page = new MarketplaceMainPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertNull(page.getLinkedAccount());

        LinkedAccount account = new LinkedAccount("id", TEST_UUID, "clerk-123", "server-1", Instant.now());
        page.setLinkedAccount(account);

        assertNotNull(page.getLinkedAccount());
        assertEquals("clerk-123", page.getLinkedAccount().getClerkUserId());
    }

    @Test
    void testSetProducts() {
        MarketplaceMainPage page = new MarketplaceMainPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertNull(page.getProducts());

        List<Product> products = Arrays.asList(
            new Product("p1", "server-1", "Product 1", "Desc", 1000, ProductType.ITEM, null, true),
            new Product("p2", "server-1", "Product 2", "Desc", 2000, ProductType.RANK, null, true)
        );
        page.setProducts(products);

        assertNotNull(page.getProducts());
        assertEquals(2, page.getProducts().size());
    }

    @Test
    void testSetPurchases() {
        MarketplaceMainPage page = new MarketplaceMainPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertNull(page.getPurchases());

        List<Purchase> purchases = Arrays.asList(
            new Purchase("pur1", "p1", "server-1", TEST_UUID.toString(), TEST_NAME, 1000, PurchaseStatus.PENDING, null, null, Instant.now()),
            new Purchase("pur2", "p2", "server-1", TEST_UUID.toString(), TEST_NAME, 2000, PurchaseStatus.COMPLETED, "checkout-1", null, Instant.now())
        );
        page.setPurchases(purchases);

        assertNotNull(page.getPurchases());
        assertEquals(2, page.getPurchases().size());
    }

    @Test
    void testGetServices() {
        MarketplaceMainPage page = new MarketplaceMainPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertEquals(mockLinkCodeService, page.getLinkCodeService());
        assertEquals(mockProductService, page.getProductService());
        assertEquals(mockPurchaseService, page.getPurchaseService());
    }
}
