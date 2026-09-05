package com.rally.notification.messaging.consumer;

import com.rally.notification.messaging.config.KafkaTopics;
import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.BaggageManager;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

@Component
public class KafkaCorrelationIdInterceptor implements RecordInterceptor<String, Object> {

    private final BaggageManager baggageManager;
    private final ThreadLocal<BaggageInScope> baggageScope = new ThreadLocal<>();

    public KafkaCorrelationIdInterceptor(BaggageManager baggageManager) {
        this.baggageManager = baggageManager;
    }

    @Override
    public ConsumerRecord<String, Object> intercept(ConsumerRecord<String, Object> record, Consumer<String, Object> consumer) {
        String correlationId = extractHeader(record, KafkaTopics.HEADER_CORRELATION_ID);
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(KafkaTopics.HEADER_CORRELATION_ID, correlationId);
            baggageScope.set(baggageManager.createBaggageInScope(KafkaTopics.HEADER_CORRELATION_ID, correlationId));
        }
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<String, Object> record, Consumer<String, Object> consumer) {
        BaggageInScope scope = baggageScope.get();
        if (scope != null) {
            scope.close();
            baggageScope.remove();
        }
        MDC.remove(KafkaTopics.HEADER_CORRELATION_ID);
    }

    private String extractHeader(ConsumerRecord<String, Object> record, String key) {
        var header = record.headers().lastHeader(key);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}