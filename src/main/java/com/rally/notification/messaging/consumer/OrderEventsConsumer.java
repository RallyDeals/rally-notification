package com.rally.notification.messaging.consumer;

import com.rally.notification.messaging.config.KafkaTopics;
import com.rally.notification.messaging.event.DealOrderCancelled;
import com.rally.notification.messaging.event.NormalOrderCancelled;
import com.rally.notification.messaging.event.OrderAuthorized;
import com.rally.notification.messaging.event.OrderCreated;
import com.rally.notification.messaging.support.EventTypes;
import com.rally.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventsConsumer {
    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.ORDER, groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, Object> record) {
        String eventType = extractHeader(record, KafkaTopics.HEADER_EVENT_TYPE);
        String messageId = extractHeader(record, KafkaTopics.HEADER_EVENT_ID);
        String correlationId = extractHeader(record, KafkaTopics.HEADER_CORRELATION_ID);

        if (eventType == null) {
            log.warn("Ignoring order event without event type header: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            return;
        }

        log.info("Kafka order event consumed: messageId={}, type={}, correlationId={}, topic={}, partition={}, offset={}",
                messageId, eventType, correlationId, record.topic(), record.partition(), record.offset());

        try {
            switch (eventType) {
                case EventTypes.ORDER_CREATED -> {
                    log.debug("Dispatching {} to notificationService.notifyOrderCreated", eventType);
                    notificationService.notifyOrderCreated((OrderCreated) record.value());
                }
                case EventTypes.ORDER_AUTHORIZED -> {
                    log.debug("Dispatching {} to notificationService.notifyOrderAuthorized", eventType);
                    notificationService.notifyOrderAuthorized((OrderAuthorized) record.value());
                }
                case EventTypes.ORDER_DEAL_CANCELLED -> {
                    log.debug("Dispatching {} to notificationService.notifyDealOrderCancelled", eventType);
                    notificationService.notifyDealOrderCancelled((DealOrderCancelled) record.value());
                }
                case EventTypes.ORDER_NORMAL_CANCELLED -> {
                    log.debug("Dispatching {} to notificationService.notifyNormalOrderCancelled", eventType);
                    notificationService.notifyNormalOrderCancelled((NormalOrderCancelled) record.value());
                }
                default -> log.warn("Unhandled order event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process order event: messageId={}, type={}, correlationId={}, topic={}, partition={}, offset={}",
                    messageId, eventType, correlationId, record.topic(), record.partition(), record.offset(), e);
            throw e;
        }

        log.info("Kafka order event processed: messageId={}, type={}, correlationId={}, topic={}",
                messageId, eventType, correlationId, record.topic());
    }

    private String extractHeader(ConsumerRecord<String, Object> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}