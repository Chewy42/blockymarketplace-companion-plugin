package com.blockymarketplace.service;

import com.blockymarketplace.api.ConvexClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class LinkCodeServiceTest {

    private TestConvexClient testClient;
    private LinkCodeService linkCodeService;

    @BeforeEach
    void setUp() {
        testClient = new TestConvexClient();
        linkCodeService = new LinkCodeService(testClient, "test-server");
    }

    @Test
    void createLinkCode_returnsLinkCodeOnSuccess() {
        UUID playerUuid = UUID.randomUUID();
        JsonObject successResult = new JsonObject();
        successResult.addProperty("_id", "created-id");
        testClient.setMutationResult(successResult);

        var result = linkCodeService.createLinkCode(playerUuid, "TestPlayer").join();

        assertNotNull(result);
        assertEquals(playerUuid, result.getPlayerUuid());
        assertEquals("TestPlayer", result.getPlayerName());
        assertEquals("test-server", result.getServerId());
        assertEquals(6, result.getCode().length());
        assertFalse(result.isExpired());
    }

    @Test
    void createLinkCode_generatesValidCode() {
        UUID playerUuid = UUID.randomUUID();
        JsonObject successResult = new JsonObject();
        testClient.setMutationResult(successResult);

        var result = linkCodeService.createLinkCode(playerUuid, "TestPlayer").join();

        assertNotNull(result);
        String code = result.getCode();
        assertEquals(6, code.length());
        assertTrue(code.matches("[ABCDEFGHJKMNPQRSTUVWXYZ23456789]+"), "Code should only contain allowed characters");
    }

    @Test
    void getLinkedAccount_returnsNullWhenNotLinked() {
        UUID playerUuid = UUID.randomUUID();
        testClient.setQueryResult(JsonNull.INSTANCE);

        var result = linkCodeService.getLinkedAccount(playerUuid).join();

        assertNull(result);
    }

    @Test
    void getLinkedAccount_returnsAccountWhenLinked() {
        UUID playerUuid = UUID.randomUUID();
        JsonObject accountJson = new JsonObject();
        accountJson.addProperty("_id", "account-id");
        accountJson.addProperty("playerUuid", playerUuid.toString());
        accountJson.addProperty("clerkUserId", "clerk_123");
        accountJson.addProperty("serverId", "test-server");
        accountJson.addProperty("linkedAt", System.currentTimeMillis());
        testClient.setQueryResult(accountJson);

        var result = linkCodeService.getLinkedAccount(playerUuid).join();

        assertNotNull(result);
        assertEquals(playerUuid, result.getPlayerUuid());
        assertEquals("clerk_123", result.getClerkUserId());
    }

    @Test
    void isPlayerLinked_returnsTrueWhenLinked() {
        UUID playerUuid = UUID.randomUUID();
        JsonObject accountJson = new JsonObject();
        accountJson.addProperty("_id", "account-id");
        accountJson.addProperty("playerUuid", playerUuid.toString());
        accountJson.addProperty("clerkUserId", "clerk_123");
        accountJson.addProperty("serverId", "test-server");
        accountJson.addProperty("linkedAt", System.currentTimeMillis());
        testClient.setQueryResult(accountJson);

        var result = linkCodeService.isPlayerLinked(playerUuid).join();

        assertTrue(result);
    }

    @Test
    void isPlayerLinked_returnsFalseWhenNotLinked() {
        UUID playerUuid = UUID.randomUUID();
        testClient.setQueryResult(JsonNull.INSTANCE);

        var result = linkCodeService.isPlayerLinked(playerUuid).join();

        assertFalse(result);
    }

    @Test
    void unlinkAccount_returnsTrueOnSuccess() {
        UUID playerUuid = UUID.randomUUID();
        JsonObject successResult = new JsonObject();
        testClient.setMutationResult(successResult);

        var result = linkCodeService.unlinkAccount(playerUuid).join();

        assertTrue(result);
    }

    private static class TestConvexClient extends ConvexClient {
        private JsonElement queryResult = JsonNull.INSTANCE;
        private JsonElement mutationResult = JsonNull.INSTANCE;

        public TestConvexClient() {
            super("https://test.convex.cloud", "test-key");
        }

        public void setQueryResult(JsonElement result) {
            this.queryResult = result;
        }

        public void setMutationResult(JsonElement result) {
            this.mutationResult = result;
        }

        @Override
        public CompletableFuture<JsonElement> query(String functionPath, Map<String, Object> args) {
            return CompletableFuture.completedFuture(queryResult);
        }

        @Override
        public CompletableFuture<JsonElement> mutation(String functionPath, Map<String, Object> args) {
            return CompletableFuture.completedFuture(mutationResult);
        }
    }
}
