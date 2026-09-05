package com.rally.notification.filters;

import com.rally.notification.messaging.config.KafkaTopics;
import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.BaggageManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component()
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private final BaggageManager baggageManager;

    public CorrelationIdFilter(BaggageManager baggageManager) {
        this.baggageManager = baggageManager;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(KafkaTopics.HEADER_CORRELATION_ID);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(KafkaTopics.HEADER_CORRELATION_ID, correlationId);

        try (BaggageInScope ignored = baggageManager.createBaggageInScope(
                KafkaTopics.HEADER_CORRELATION_ID, correlationId)) {

            response.addHeader(KafkaTopics.HEADER_CORRELATION_ID, correlationId);

            try {
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove(KafkaTopics.HEADER_CORRELATION_ID);
            }
        }
    }
}