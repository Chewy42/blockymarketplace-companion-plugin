package com.blockymarketplace.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebhookSignatureValidatorTest {

    @Test
    void validSignatureReturnsTrue() {
        String secret = "test-secret";
        String payload = "{\"type\":\"test\"}";

        WebhookSignatureValidator validator = new WebhookSignatureValidator(secret);

        String validSignature = computeSignature(secret, payload);
        assertTrue(validator.isValid(payload, validSignature));
    }

    @Test
    void invalidSignatureReturnsFalse() {
        String secret = "test-secret";
        String payload = "{\"type\":\"test\"}";

        WebhookSignatureValidator validator = new WebhookSignatureValidator(secret);

        assertFalse(validator.isValid(payload, "invalid-signature"));
    }

    @Test
    void sha256PrefixIsStripped() {
        String secret = "test-secret";
        String payload = "{\"type\":\"test\"}";

        WebhookSignatureValidator validator = new WebhookSignatureValidator(secret);

        String validSignature = computeSignature(secret, payload);
        assertTrue(validator.isValid(payload, "sha256=" + validSignature));
    }

    @Test
    void nullInputsReturnFalse() {
        WebhookSignatureValidator validator = new WebhookSignatureValidator("secret");

        assertFalse(validator.isValid(null, "signature"));
        assertFalse(validator.isValid("payload", null));

        WebhookSignatureValidator nullSecretValidator = new WebhookSignatureValidator(null);
        assertFalse(nullSecretValidator.isValid("payload", "signature"));
    }

    private String computeSignature(String secret, String payload) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey =
                new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
