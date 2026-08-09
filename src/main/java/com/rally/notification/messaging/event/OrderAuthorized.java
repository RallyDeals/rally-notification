package com.rally.notification.messaging.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderAuthorized(UUID orderId, UUID dealId, UUID userId, BigDecimal totalPrice) {
}
