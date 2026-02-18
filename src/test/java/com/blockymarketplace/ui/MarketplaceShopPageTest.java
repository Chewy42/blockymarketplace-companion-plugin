package com.blockymarketplace.ui;

import com.blockymarketplace.model.LinkedAccount;
import com.blockymarketplace.model.Product;
import com.blockymarketplace.model.ProductType;
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

class MarketplaceShopPageTest {

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
        MarketplaceShopPage page = new MarketplaceShopPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertNotNull(page.getProducts());
        assertTrue(page.getProducts().isEmpty());
    }

    @Test
    void testSetProducts() {
        MarketplaceShopPage page = new MarketplaceShopPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        List<Product> products = Arrays.asList(
            new Product("p1", "server-1", "Sword", "A sharp sword", 1000, ProductType.ITEM, null, true),
            new Product("p2", "server-1", "VIP Rank", "VIP access", 5000, ProductType.RANK, null, true)
        );
        page.setProducts(products);

        assertEquals(2, page.getProducts().size());
        assertEquals("Sword", page.getProducts().get(0).getName());
    }

    @Test
    void testSetProductsNull() {
        MarketplaceShopPage page = new MarketplaceShopPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        page.setProducts(null);
        assertNotNull(page.getProducts());
        assertTrue(page.getProducts().isEmpty());
    }

    @Test
    void testSetLinkedAccount() {
        MarketplaceShopPage page = new MarketplaceShopPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        assertNull(page.getLinkedAccount());

        LinkedAccount account = new LinkedAccount("id", TEST_UUID, "clerk-123", "server-1", Instant.now());
        page.setLinkedAccount(account);

        assertNotNull(page.getLinkedAccount());
    }

    @Test
    void testProductTypes() {
        MarketplaceShopPage page = new MarketplaceShopPage(
            mockPlayerRef, mockLinkCodeService, mockProductService, mockPurchaseService, null
        );

        List<Product> products = Arrays.asList(
            new Product("p1", "server-1", "Item", null, 100, ProductType.ITEM, null, true),
            new Product("p2", "server-1", "Rank", null, 200, ProductType.RANK, null, true),
            new Product("p3", "server-1", "Currency", null, 300, ProductType.CURRENCY, null, true),
            new Product("p4", "server-1", "Command", null, 400, ProductType.COMMAND, null, true)
        );
        page.setProducts(products);

        assertEquals(4, page.getProducts().size());
    }
}
