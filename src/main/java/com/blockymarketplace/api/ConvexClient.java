package com.blockymarketplace.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConvexClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Logger LOGGER = Logger.getLogger(ConvexClient.class.getName());

    private final String convexUrl;
    private final String serverApiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public ConvexClient(String convexUrl, String serverApiKey) {
        this.convexUrl = convexUrl.endsWith("/") ? convexUrl.substring(0, convexUrl.length() - 1) : convexUrl;
        this.serverApiKey = serverApiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public CompletableFuture<JsonElement> query(String functionPath, Map<String, Object> args) {
        return executeFunction("query", functionPath, args);
    }

    public CompletableFuture<JsonElement> mutation(String functionPath, Map<String, Object> args) {
        return executeFunction("mutation", functionPath, args);
    }

    public CompletableFuture<JsonElement> action(String functionPath, Map<String, Object> args) {
        return executeFunction("action", functionPath, args);
    }

    private CompletableFuture<JsonElement> executeFunction(String type, String functionPath, Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("path", functionPath);
                requestBody.add("args", gson.toJsonTree(args != null ? args : Map.of()));
                requestBody.addProperty("format", "json");

                String endpoint = convexUrl + "/api/" + type;

                Request.Builder requestBuilder = new Request.Builder()
                        .url(endpoint)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), JSON));

                if (serverApiKey != null && !serverApiKey.isBlank() && serverApiKey.contains(".")) {
                    requestBuilder.addHeader("Authorization", "Bearer " + serverApiKey);
                }

                Request request = requestBuilder.build();

                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "{}";

                    if (!response.isSuccessful()) {
                        LOGGER.log(Level.WARNING, "Convex {0} failed: {1} - {2}",
                                new Object[]{type, response.code(), responseBody});
                        throw new ConvexException("Request failed with status " + response.code() + ": " + responseBody);
                    }

                    JsonObject result = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (result.has("status") && "error".equals(result.get("status").getAsString())) {
                        String errorMessage = result.has("errorMessage")
                                ? result.get("errorMessage").getAsString()
                                : "Unknown error";
                        throw new ConvexException(errorMessage);
                    }

                    if (result.has("value")) {
                        return result.get("value");
                    }
                    return result;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Convex request failed", e);
                throw new ConvexException("Network error: " + e.getMessage(), e);
            }
        });
    }

    public void shutdown() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    public static class ConvexException extends RuntimeException {
        public ConvexException(String message) {
            super(message);
        }

        public ConvexException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
