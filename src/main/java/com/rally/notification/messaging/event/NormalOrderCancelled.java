package com.rally.notification.messaging.event;

import com.rally.notification.dto.OrderProductResponse;
import com.rally.notification.dto.CancelReason;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record NormalOrderCancelled(UUID orderId, UUID userId, CancelReason cancelReason, List<OrderProductResponse> items,
                                    BigDecimal totalPrice, String paymentErrorMessage) {
}
