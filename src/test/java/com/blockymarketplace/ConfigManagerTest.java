package com.blockymarketplace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void loadConfig_createsTemplateWhenMissing() {
        ConfigManager manager = new ConfigManager();
        Path configPath = tempDir.resolve("config.json");

        ConfigManager.MarketplaceConfig result = manager.loadConfig(configPath);

        assertNull(result);
        assertTrue(Files.exists(configPath));
    }

    @Test
    void loadConfig_parsesValidConfig() throws IOException {
        ConfigManager manager = new ConfigManager();
        Path configPath = tempDir.resolve("config.json");
        String validConfig = """
                {
                  "server": {
                    "id": "test-server",
                    "apiKey": "test-api-key"
                  },
                  "convex": {
                    "url": "https://example.convex.cloud",
                    "deploymentToken": "token123"
                  },
                  "webhook": {
                    "enabled": true,
                    "port": 9000,
                    "polarSecret": "whsec_test"
                  }
                }
                """;
        Files.writeString(configPath, validConfig);

        ConfigManager.MarketplaceConfig result = manager.loadConfig(configPath);

        assertNotNull(result);
        assertEquals("test-server", result.server.id);
        assertEquals("test-api-key", result.server.apiKey);
        assertEquals("https://example.convex.cloud", result.convex.url);
        assertEquals("token123", result.convex.deploymentToken);
        assertTrue(result.webhook.enabled);
        assertEquals(9000, result.webhook.port);
        assertEquals("whsec_test", result.webhook.polarSecret);
    }

    @Test
    void isValidConfig_returnsFalseForNull() {
        assertFalse(ConfigManager.isValidConfig(null));
    }

    @Test
    void isValidConfig_returnsFalseForMissingServer() {
        ConfigManager.MarketplaceConfig config = new ConfigManager.MarketplaceConfig();
        config.convex = new ConfigManager.ConvexConfig();
        config.convex.url = "https://example.convex.cloud";

        assertFalse(ConfigManager.isValidConfig(config));
    }

    @Test
    void isValidConfig_returnsFalseForMissingConvex() {
        ConfigManager.MarketplaceConfig config = new ConfigManager.MarketplaceConfig();
        config.server = new ConfigManager.ServerConfig();
        config.server.apiKey = "test-key";

        assertFalse(ConfigManager.isValidConfig(config));
    }

    @Test
    void isValidConfig_returnsFalseForEmptyApiKey() {
        ConfigManager.MarketplaceConfig config = new ConfigManager.MarketplaceConfig();
        config.server = new ConfigManager.ServerConfig();
        config.server.apiKey = "";
        config.convex = new ConfigManager.ConvexConfig();
        config.convex.url = "https://example.convex.cloud";

        assertFalse(ConfigManager.isValidConfig(config));
    }

    @Test
    void isValidConfig_returnsFalseForEmptyConvexUrl() {
        ConfigManager.MarketplaceConfig config = new ConfigManager.MarketplaceConfig();
        config.server = new ConfigManager.ServerConfig();
        config.server.apiKey = "test-key";
        config.convex = new ConfigManager.ConvexConfig();
        config.convex.url = "";

        assertFalse(ConfigManager.isValidConfig(config));
    }

    @Test
    void isValidConfig_returnsTrueForValidConfig() {
        ConfigManager.MarketplaceConfig config = new ConfigManager.MarketplaceConfig();
        config.server = new ConfigManager.ServerConfig();
        config.server.apiKey = "test-key";
        config.convex = new ConfigManager.ConvexConfig();
        config.convex.url = "https://example.convex.cloud";

        assertTrue(ConfigManager.isValidConfig(config));
    }
}
