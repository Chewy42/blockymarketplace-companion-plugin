package com.blockymarketplace;

import com.blockymarketplace.api.ConvexClient;
import com.blockymarketplace.command.BuyCommand;
import com.blockymarketplace.command.ItemIdCommand;
import com.blockymarketplace.command.LinkCommandDeliveryCommand;
import com.blockymarketplace.command.LinkCommand;
import com.blockymarketplace.command.LinkItemCommand;
import com.blockymarketplace.command.LinkRankCommand;
import com.blockymarketplace.command.LinkStatusCommand;
import com.blockymarketplace.command.PurchasesCommand;
import com.blockymarketplace.command.RedeemPlayerCommand;
import com.blockymarketplace.command.RedeemCommand;
import com.blockymarketplace.command.ShopCommand;
import com.blockymarketplace.command.UnlinkCommand;
import com.blockymarketplace.listener.PlayerEventListener;
import com.blockymarketplace.service.DeliveryService;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.blockymarketplace.service.LinkCodeService;
import com.blockymarketplace.service.ProductService;
import com.blockymarketplace.service.PurchaseService;
import com.blockymarketplace.webhook.PolarWebhookHandler;
import com.blockymarketplace.webhook.WebhookServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.logging.Level;

public class BlockyMarketplacePlugin extends JavaPlugin {
    private ConvexClient convexClient;
    private ConfigManager.MarketplaceConfig config;
    private LinkCodeService linkCodeService;
    private ProductService productService;
    private PurchaseService purchaseService;
    private DeliveryService deliveryService;
    private WebhookServer webhookServer;
    private PlayerEventListener playerEventListener;

    public BlockyMarketplacePlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("BlockyMarketplace: Setup phase...");
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("BlockyMarketplace: Starting...");

        config = new ConfigManager().loadConfig(getDataDirectory().resolve("config.json"));
        if (!ConfigManager.isValidConfig(config)) {
            getLogger().at(Level.WARNING)
                    .log("BlockyMarketplace: Missing or incomplete config.json; created template at: "
                            + getDataDirectory().resolve("config.json").toAbsolutePath()
                            + " (fill in server.apiKey and convex.url). Marketplace disabled.");
            registerDisabledCommands();
            return;
        }

        try {
            convexClient = new ConvexClient(config.convex.url, config.server.apiKey);
            getLogger().at(Level.INFO).log("BlockyMarketplace: Convex client initialized.");

            String serverId = config.server.id != null ? config.server.id : "default";
            linkCodeService = new LinkCodeService(convexClient, serverId);
            productService = new ProductService(convexClient, serverId, config.server.apiKey);
            purchaseService = new PurchaseService(convexClient, serverId);
            deliveryService = new DeliveryService(convexClient, purchaseService, productService);

            registerCommands();
            registerListeners();
            startWebhookServer();

            getLogger().at(Level.INFO).log("BlockyMarketplace: Started successfully!");
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e)
                    .log("BlockyMarketplace: Failed to initialize; marketplace disabled.");
            registerDisabledCommands();
        }
    }

    private void registerCommands() {
        String webappUrl = config.webapp != null ? config.webapp.url : null;
        getLogger().at(Level.INFO).log("BlockyMarketplace: Registering commands...");
        getCommandRegistry().registerCommand(new LinkCommand(linkCodeService, webappUrl));
        getCommandRegistry().registerCommand(new LinkStatusCommand(linkCodeService));
        getCommandRegistry().registerCommand(new UnlinkCommand(linkCodeService));
        getCommandRegistry().registerCommand(new ShopCommand(productService));
        getCommandRegistry().registerCommand(new BuyCommand(linkCodeService, productService, purchaseService, webappUrl));
        getCommandRegistry().registerCommand(new PurchasesCommand(purchaseService));
        getCommandRegistry().registerCommand(new RedeemCommand(deliveryService));
        getCommandRegistry().registerCommand(new RedeemPlayerCommand(deliveryService));
        getCommandRegistry().registerCommand(new ItemIdCommand());
        getCommandRegistry().registerCommand(new LinkItemCommand(productService));
        getCommandRegistry().registerCommand(new LinkRankCommand(productService));
        getCommandRegistry().registerCommand(new LinkCommandDeliveryCommand(productService));
    }

    private void registerListeners() {
        getLogger().at(Level.INFO).log("BlockyMarketplace: Registering event listeners...");
        playerEventListener = new PlayerEventListener(deliveryService);
        getEventRegistry().register(PlayerConnectEvent.class, playerEventListener::onPlayerConnect);
    }

    private void registerDisabledCommands() {
        getLogger().at(Level.INFO).log("BlockyMarketplace: Registering commands (disabled mode)...");
        getCommandRegistry().registerCommand(new LinkCommand(null, null));
        getCommandRegistry().registerCommand(new LinkStatusCommand(null));
        getCommandRegistry().registerCommand(new UnlinkCommand(null));
        getCommandRegistry().registerCommand(new ShopCommand(null));
        getCommandRegistry().registerCommand(new BuyCommand(null, null, null, null));
        getCommandRegistry().registerCommand(new PurchasesCommand(null));
        getCommandRegistry().registerCommand(new RedeemCommand(null));
        getCommandRegistry().registerCommand(new RedeemPlayerCommand(null));
        getCommandRegistry().registerCommand(new ItemIdCommand());
        getCommandRegistry().registerCommand(new LinkItemCommand(null));
        getCommandRegistry().registerCommand(new LinkRankCommand(null));
        getCommandRegistry().registerCommand(new LinkCommandDeliveryCommand(null));
    }

    private void startWebhookServer() {
        if (config.webhook == null || !config.webhook.enabled) {
            getLogger().at(Level.INFO).log("BlockyMarketplace: Webhook server disabled in config.");
            return;
        }

        if (config.webhook.polarSecret == null || config.webhook.polarSecret.isBlank()) {
            getLogger().at(Level.WARNING).log("BlockyMarketplace: Webhook enabled but polarSecret is missing.");
            return;
        }

        try {
            int port = config.webhook.port > 0 ? config.webhook.port : 8080;
            PolarWebhookHandler handler = new PolarWebhookHandler(purchaseService, deliveryService);
            webhookServer = new WebhookServer(port, config.webhook.polarSecret, handler);
            webhookServer.start();
            getLogger().at(Level.INFO).log("BlockyMarketplace: Webhook server started on port " + port);
        } catch (Exception e) {
            getLogger().at(Level.WARNING).withCause(e)
                    .log("BlockyMarketplace: Failed to start webhook server. Payments will not auto-process.");
        }
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("BlockyMarketplace: Shutting down...");
        if (webhookServer != null) {
            webhookServer.stop();
        }
        if (convexClient != null) {
            convexClient.shutdown();
        }
    }

    public ConvexClient getConvexClient() {
        return convexClient;
    }

    public ConfigManager.MarketplaceConfig getConfig() {
        return config;
    }

    public LinkCodeService getLinkCodeService() {
        return linkCodeService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public PurchaseService getPurchaseService() {
        return purchaseService;
    }

    public DeliveryService getDeliveryService() {
        return deliveryService;
    }
}
