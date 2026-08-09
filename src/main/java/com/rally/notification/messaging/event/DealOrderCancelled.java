package com.rally.notification.messaging.event;

import com.rally.notification.dto.CancelReason;
import com.rally.notification.dto.OrderProductResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record DealOrderCancelled(UUID orderId, UUID dealId, UUID participantId, UUID userId,
                                 CancelReason reason, List<OrderProductResponse> items, BigDecimal totalPrice) {
}
