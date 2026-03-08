package com.blockymarketplace.ui;

/**
 * Lightweight event payload used by legacy marketplace UI tests.
 * <p>
 * The runtime custom UI system is disabled for compatibility, so this class keeps
 * only the minimal data shape expected by tests and command-layer callers.
 */
public class MarketplaceEventData {
    /** Placeholder retained for compatibility with previous API/tests. */
    public static final Object CODEC = new Object();

    public String action;
    public String target;
}
