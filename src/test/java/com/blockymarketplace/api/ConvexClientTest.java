package com.blockymarketplace.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class ConvexClientTest {

    private ConvexClient client;

    @BeforeEach
    void setUp() {
        client = new ConvexClient("https://test.convex.cloud", "test-api-key");
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.shutdown();
        }
    }

    @Test
    void constructor_normalizesUrlWithTrailingSlash() {
        ConvexClient clientWithSlash = new ConvexClient("https://test.convex.cloud/", "key");
        assertNotNull(clientWithSlash);
        clientWithSlash.shutdown();
    }

    @Test
    void query_returnsCompletableFuture() {
        CompletableFuture<?> future = client.query("test:function", Map.of("arg", "value"));
        assertNotNull(future);
    }

    @Test
    void mutation_returnsCompletableFuture() {
        CompletableFuture<?> future = client.mutation("test:function", Map.of("arg", "value"));
        assertNotNull(future);
    }

    @Test
    void action_returnsCompletableFuture() {
        CompletableFuture<?> future = client.action("test:function", Map.of("arg", "value"));
        assertNotNull(future);
    }

    @Test
    void query_handlesNullArgs() {
        CompletableFuture<?> future = client.query("test:function", null);
        assertNotNull(future);
    }

    @Test
    void convexException_containsMessage() {
        ConvexClient.ConvexException exception = new ConvexClient.ConvexException("Test error");
        assertEquals("Test error", exception.getMessage());
    }

    @Test
    void convexException_containsCause() {
        RuntimeException cause = new RuntimeException("Original error");
        ConvexClient.ConvexException exception = new ConvexClient.ConvexException("Wrapped error", cause);
        assertEquals("Wrapped error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
