package com.blockymarketplace.webhook;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WebhookServer {
    private static final Logger LOGGER = Logger.getLogger(WebhookServer.class.getName());
    private static final String POLAR_SIGNATURE_HEADER = "webhook-signature";

    private final int port;
    private final WebhookSignatureValidator signatureValidator;
    private final PolarWebhookHandler webhookHandler;
    private Javalin app;

    public WebhookServer(int port, String polarSecret, PolarWebhookHandler webhookHandler) {
        this.port = port;
        this.signatureValidator = new WebhookSignatureValidator(polarSecret);
        this.webhookHandler = webhookHandler;
    }

    public void start() {
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        });

        app.get("/health", this::handleHealth);
        app.post("/webhook/polar", this::handlePolarWebhook);

        app.start(port);
        LOGGER.log(Level.INFO, "Webhook server started on port {0}", port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
            LOGGER.log(Level.INFO, "Webhook server stopped");
        }
    }

    private void handleHealth(Context ctx) {
        ctx.json(new HealthResponse("ok"));
    }

    private void handlePolarWebhook(Context ctx) {
        String signature = ctx.header(POLAR_SIGNATURE_HEADER);
        String payload = ctx.body();

        if (!signatureValidator.isValid(payload, signature)) {
            LOGGER.log(Level.WARNING, "Invalid webhook signature received");
            ctx.status(401).json(new ErrorResponse("Invalid signature"));
            return;
        }

        webhookHandler.handle(payload).thenAccept(result -> {
            if (result.success()) {
                ctx.status(200).json(new SuccessResponse(result.message()));
            } else {
                ctx.status(400).json(new ErrorResponse(result.message()));
            }
        }).exceptionally(e -> {
            LOGGER.log(Level.SEVERE, "Error handling webhook", e);
            ctx.status(500).json(new ErrorResponse("Internal error"));
            return null;
        });
    }

    public boolean isRunning() {
        return app != null;
    }

    private record HealthResponse(String status) {}
    private record SuccessResponse(String message) {}
    private record ErrorResponse(String error) {}
}
