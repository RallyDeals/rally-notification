package com.rally.notification.messaging.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRegistered(UUID userId, String email, String role, String createdAt) {
}
