package org.example.rental.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwsService {

    @Value("${jws.secret}")
    private String jwsSecret;

    @Autowired
    private ObjectMapper objectMapper;

    public String createVerificationToken(String userId) {
        try {
            // Hashujemy ID użytkownika
            String hashedUserId = hashUserId(userId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("hashedUserId", hashedUserId);
            payload.put("timestamp", System.currentTimeMillis());

            return createSignature(payload);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create verification token", e);
        }
    }

    public boolean verifyUserIdToken(String token, String userId) {
        try {
            Map<String, Object> payload = extractPayload(token);
            if (payload == null) return false;

            String hashedUserIdFromToken = (String) payload.get("hashedUserId");
            String hashedUserIdProvided = hashUserId(userId);

            return hashedUserIdFromToken != null &&
                    hashedUserIdFromToken.equals(hashedUserIdProvided);

        } catch (Exception e) {
            return false;
        }
    }

    private String hashUserId(String userId) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(jwsSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(userId.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to hash user ID", e);
        }
    }

    public String createSignature(Object data) {
        try {
            String payload = objectMapper.writeValueAsString(data);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

            String header = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    "{\"alg\":\"HS256\",\"typ\":\"JWS\"}".getBytes()
            );

            String signingInput = header + "." + encodedPayload;
            String signature = calculateHMAC(signingInput);

            return signingInput + "." + signature;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create JWS signature", e);
        }
    }

    public boolean verifySignature(String jws, Object expectedData) {
        try {
            String[] parts = jws.split("\\.");
            if (parts.length != 3) return false;

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String signingInput = header + "." + payload;
            String expectedSignature = calculateHMAC(signingInput);
            if (!signature.equals(expectedSignature)) return false;

            String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));
            String expectedPayload = objectMapper.writeValueAsString(expectedData);

            return decodedPayload.equals(expectedPayload);

        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> extractPayload(String jws) {
        try {
            String[] parts = jws.split("\\.");
            if (parts.length != 3) return null;

            String payload = parts[1];
            String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));

            return objectMapper.readValue(decodedPayload, Map.class);

        } catch (Exception e) {
            return null;
        }
    }

    private String calculateHMAC(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(jwsSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}