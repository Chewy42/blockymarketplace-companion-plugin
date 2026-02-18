package com.blockymarketplace.ui;

import com.blockymarketplace.model.Purchase;
import com.blockymarketplace.model.PurchaseStatus;
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

class MarketplacePurchasesPageTest {

    @Mock
    private PlayerRef mockPlayerRef;

    @Mock
    private PurchaseService mockPurchaseService;

    @Mock
    private ProductService mockProductService;

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
        MarketplacePurchasesPage page = new MarketplacePurchasesPage(
            mockPlayerRef, mockPurchaseService, mockProductService
        );

        assertNotNull(page.getPurchases());
        assertTrue(page.getPurchases().isEmpty());
    }

    @Test
    void testSetPurchases() {
        MarketplacePurchasesPage page = new MarketplacePurchasesPage(
            mockPlayerRef, mockPurchaseService, mockProductService
        );

        List<Purchase> purchases = Arrays.asList(
            new Purchase("pur1", "p1", "server-1", TEST_UUID.toString(), TEST_NAME, 1000, PurchaseStatus.PENDING, null, null, Instant.now()),
            new Purchase("pur2", "p2", "server-1", TEST_UUID.toString(), TEST_NAME, 2000, PurchaseStatus.COMPLETED, "checkout-1", null, Instant.now())
        );
        page.setPurchases(purchases);

        assertEquals(2, page.getPurchases().size());
    }

    @Test
    void testSetPurchasesNull() {
        MarketplacePurchasesPage page = new MarketplacePurchasesPage(
            mockPlayerRef, mockPurchaseService, mockProductService
        );

        page.setPurchases(null);
        assertNotNull(page.getPurchases());
        assertTrue(page.getPurchases().isEmpty());
    }

    @Test
    void testSetProductName() {
        MarketplacePurchasesPage page = new MarketplacePurchasesPage(
            mockPlayerRef, mockPurchaseService, mockProductService
        );

        page.setProductName("prod-123", "Diamond Sword");
        page.setProductName("prod-456", "VIP Rank");

        assertNotNull(page);
    }

    @Test
    void testSetProductNameNull() {
        MarketplacePurchasesPage page = new MarketplacePurchasesPage(
            mockPlayerRef, mockPurchaseService, mockProductService
        );

        page.setProductName(null, "Test");
        page.setProductName("test", null);
        page.setProductName(null, null);

        assertNotNull(page);
    }

    @Test
    void testPurchaseStatuses() {
        MarketplacePurchasesPage page = new MarketplacePurchasesPage(
            mockPlayerRef, mockPurchaseService, mockProductService
        );

        List<Purchase> purchases = Arrays.asList(
            new Purchase("pur1", "p1", "server-1", TEST_UUID.toString(), TEST_NAME, 1000, PurchaseStatus.PENDING, null, null, Instant.now()),
            new Purchase("pur2", "p2", "server-1", TEST_UUID.toString(), TEST_NAME, 2000, PurchaseStatus.COMPLETED, "checkout", null, Instant.now()),
            new Purchase("pur3", "p3", "server-1", TEST_UUID.toString(), TEST_NAME, 3000, PurchaseStatus.FAILED, null, null, Instant.now()),
            new Purchase("pur4", "p4", "server-1", TEST_UUID.toString(), TEST_NAME, 4000, PurchaseStatus.COMPLETED, "checkout", Instant.now(), Instant.now())
        );
        page.setPurchases(purchases);

        assertEquals(4, page.getPurchases().size());
        assertFalse(page.getPurchases().get(0).isCompleted());
        assertTrue(page.getPurchases().get(1).isCompleted());
        assertEquals(PurchaseStatus.FAILED, page.getPurchases().get(2).getStatus());
        assertTrue(page.getPurchases().get(3).isDelivered());
    }
}
