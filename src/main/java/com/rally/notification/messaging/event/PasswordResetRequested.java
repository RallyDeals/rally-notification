package com.rally.notification.messaging.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PasswordResetRequested(UUID userId, String email, String otp) {
}
