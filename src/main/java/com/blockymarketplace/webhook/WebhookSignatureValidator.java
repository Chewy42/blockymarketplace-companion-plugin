package com.blockymarketplace.webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class WebhookSignatureValidator {
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String secret;

    public WebhookSignatureValidator(String secret) {
        this.secret = secret;
    }

    public boolean isValid(String payload, String signature) {
        if (signature == null || payload == null || secret == null) {
            return false;
        }

        try {
            String computed = computeSignature(payload);
            return constantTimeEquals(computed, normalizeSignature(signature));
        } catch (Exception e) {
            return false;
        }
    }

    private String computeSignature(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKey);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    private String normalizeSignature(String signature) {
        if (signature.startsWith("sha256=")) {
            return signature.substring(7);
        }
        return signature;
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
