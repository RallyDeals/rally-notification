package com.rally.notification.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class OtpDecryptor {

    private final TextEncryptor encryptor;
    private final boolean configured;

    public OtpDecryptor(
            @Value("${app.otp.encryption.password}") String password,
            @Value("${app.otp.encryption.salt}") String salt) {
        if (isBlank(password) || isBlank(salt)) {
            this.encryptor = null;
            this.configured = false;
            return;
        }
        this.encryptor = Encryptors.text(password, salt);
        this.configured = true;
    }

    public boolean isConfigured() {
        return configured;
    }

    public String decrypt(String encryptedCode) {
        if (!configured) {
            throw new IllegalStateException("OTP encryption not configured");
        }
        try {
            return encryptor.decrypt(encryptedCode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt OTP", e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
