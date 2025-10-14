package com.maternity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;
    private static final int RATE_LIMIT_SECONDS = 60;

    // Store verification codes with expiration time
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    // Store last send time for rate limiting
    private final Map<String, LocalDateTime> rateLimitStore = new ConcurrentHashMap<>();

    private static class CodeEntry {
        String code;
        LocalDateTime expiresAt;
        int attempts;

        CodeEntry(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.attempts = 0;
        }
    }

    /**
     * Generate a 6-digit verification code
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Get the key for storing verification code
     */
    private String getKey(String countryCode, String phoneNumber) {
        return countryCode + ":" + phoneNumber;
    }

    /**
     * Check if rate limit is exceeded
     */
    public boolean isRateLimitExceeded(String countryCode, String phoneNumber) {
        String key = getKey(countryCode, phoneNumber);
        LocalDateTime lastSentTime = rateLimitStore.get(key);

        if (lastSentTime != null) {
            LocalDateTime now = LocalDateTime.now();
            long secondsSinceLastSend = java.time.Duration.between(lastSentTime, now).getSeconds();
            return secondsSinceLastSend < RATE_LIMIT_SECONDS;
        }

        return false;
    }

    /**
     * Send verification code (store in memory)
     */
    public String sendCode(String countryCode, String phoneNumber) {
        String key = getKey(countryCode, phoneNumber);

        // Generate code
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        // Store code
        codeStore.put(key, new CodeEntry(code, expiresAt));
        rateLimitStore.put(key, LocalDateTime.now());

        log.info("📱 Verification code generated for {}: {} (expires at: {})",
                 key, code, expiresAt);

        // In production, send SMS here
        // For development, we'll just log it
        log.info("🔔 SMS would be sent to {} {} with code: {}", countryCode, phoneNumber, code);

        return code;
    }

    /**
     * Verify the code
     */
    public boolean verifyCode(String countryCode, String phoneNumber, String code) {
        String key = getKey(countryCode, phoneNumber);
        CodeEntry entry = codeStore.get(key);

        if (entry == null) {
            log.warn("❌ No verification code found for {}", key);
            return false;
        }

        // Check expiration
        if (LocalDateTime.now().isAfter(entry.expiresAt)) {
            log.warn("❌ Verification code expired for {}", key);
            codeStore.remove(key);
            return false;
        }

        // Check attempts (max 3)
        if (entry.attempts >= 3) {
            log.warn("❌ Too many verification attempts for {}", key);
            codeStore.remove(key);
            return false;
        }

        // Verify code
        entry.attempts++;
        if (!entry.code.equals(code)) {
            log.warn("❌ Invalid verification code for {} (attempt {})", key, entry.attempts);
            return false;
        }

        // Success - remove code to prevent reuse
        codeStore.remove(key);
        log.info("✅ Verification code verified successfully for {}", key);
        return true;
    }

    /**
     * Clean up expired codes (should be called periodically)
     */
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        codeStore.entrySet().removeIf(entry ->
            now.isAfter(entry.getValue().expiresAt)
        );

        // Also cleanup old rate limit entries (older than 1 day)
        rateLimitStore.entrySet().removeIf(entry ->
            now.minusDays(1).isAfter(entry.getValue())
        );
    }
}
