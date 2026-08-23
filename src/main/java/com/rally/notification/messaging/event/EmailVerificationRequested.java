package com.rally.notification.messaging.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EmailVerificationRequested(UUID userId, String email, String otp) {
}
