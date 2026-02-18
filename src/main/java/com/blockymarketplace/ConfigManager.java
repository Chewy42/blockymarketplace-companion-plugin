package com.blockymarketplace;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigManager {
    private final Gson gson = new Gson();

    public static class ServerConfig {
        public String id;
        public String apiKey;
    }

    public static class ConvexConfig {
        public String url;
        public String deploymentToken;
    }

    public static class WebhookConfig {
        public boolean enabled;
        public int port;
        public String polarSecret;
    }

    public static class WebappConfig {
        public String url;
    }

    public static class MarketplaceConfig {
        public ServerConfig server;
        public ConvexConfig convex;
        public WebhookConfig webhook;
        public WebappConfig webapp;
    }

    public MarketplaceConfig loadConfig(Path configPath) {
        MarketplaceConfig envConfig = loadFromEnvironment();
        if (isValidConfig(envConfig)) {
            return envConfig;
        }

        if (!Files.exists(configPath)) {
            try {
                createTemplateConfig(configPath);
            } catch (IOException ignored) {
            }
            return null;
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            return gson.fromJson(reader, MarketplaceConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private MarketplaceConfig loadFromEnvironment() {
        String serverId = System.getenv("BLOCKY_SERVER_ID");
        String serverApiKey = System.getenv("BLOCKY_SERVER_API_KEY");
        String convexUrl = System.getenv("BLOCKY_CONVEX_URL");
        String convexToken = System.getenv("BLOCKY_CONVEX_TOKEN");
        String webhookPort = System.getenv("BLOCKY_WEBHOOK_PORT");
        String polarSecret = System.getenv("BLOCKY_POLAR_SECRET");

        if (convexUrl == null && serverApiKey == null) {
            return null;
        }

        MarketplaceConfig config = new MarketplaceConfig();
        config.server = new ServerConfig();
        config.server.id = serverId;
        config.server.apiKey = serverApiKey;

        config.convex = new ConvexConfig();
        config.convex.url = convexUrl;
        config.convex.deploymentToken = convexToken;

        config.webhook = new WebhookConfig();
        config.webhook.enabled = polarSecret != null && !polarSecret.isBlank();
        config.webhook.port = webhookPort != null ? Integer.parseInt(webhookPort) : 8080;
        config.webhook.polarSecret = polarSecret;

        String webappUrl = System.getenv("BLOCKY_WEBAPP_URL");
        if (webappUrl != null && !webappUrl.isBlank()) {
            config.webapp = new WebappConfig();
            config.webapp.url = webappUrl;
        }

        return config;
    }

    public static boolean isValidConfig(MarketplaceConfig config) {
        if (config == null) {
            return false;
        }
        if (config.server == null || config.convex == null) {
            return false;
        }
        return isNonBlank(config.server.apiKey) && isNonBlank(config.convex.url);
    }

    private static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static void createTemplateConfig(Path configPath) throws IOException {
        Path parent = configPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String template = """
                {
                  "server": {
                    "id": "",
                    "apiKey": ""
                  },
                  "convex": {
                    "url": "",
                    "deploymentToken": ""
                  },
                  "webhook": {
                    "enabled": false,
                    "port": 8080,
                    "polarSecret": ""
                  },
                  "webapp": {
                    "url": ""
                  }
                }
                """;
        Files.writeString(configPath, template, StandardOpenOption.CREATE_NEW);
    }
}
