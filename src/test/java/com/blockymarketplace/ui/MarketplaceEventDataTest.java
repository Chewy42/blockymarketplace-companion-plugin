package com.blockymarketplace.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarketplaceEventDataTest {

    @Test
    void testCodecNotNull() {
        assertNotNull(MarketplaceEventData.CODEC);
    }

    @Test
    void testDefaultValues() {
        MarketplaceEventData data = new MarketplaceEventData();
        assertNull(data.action);
        assertNull(data.target);
    }

    @Test
    void testSetAction() {
        MarketplaceEventData data = new MarketplaceEventData();
        data.action = "navigate";
        assertEquals("navigate", data.action);
    }

    @Test
    void testSetTarget() {
        MarketplaceEventData data = new MarketplaceEventData();
        data.target = "profile";
        assertEquals("profile", data.target);
    }

    @Test
    void testSetBothFields() {
        MarketplaceEventData data = new MarketplaceEventData();
        data.action = "buy";
        data.target = "product-123";
        assertEquals("buy", data.action);
        assertEquals("product-123", data.target);
    }

    @Test
    void testActionValues() {
        String[] validActions = {"navigate", "buy", "link", "unlink", "refresh", "close", "back", "generateCode"};

        for (String action : validActions) {
            MarketplaceEventData data = new MarketplaceEventData();
            data.action = action;
            assertEquals(action, data.action);
        }
    }

    @Test
    void testTargetValues() {
        String[] validTargets = {"profile", "shop", "purchases", "product-1", "product-abc123"};

        for (String target : validTargets) {
            MarketplaceEventData data = new MarketplaceEventData();
            data.target = target;
            assertEquals(target, data.target);
        }
    }
}
