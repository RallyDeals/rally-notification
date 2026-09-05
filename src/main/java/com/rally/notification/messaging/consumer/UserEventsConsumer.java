package com.rally.notification.messaging.consumer;

import com.rally.notification.messaging.config.KafkaTopics;
import com.rally.notification.messaging.event.EmailVerificationRequested;
import com.rally.notification.messaging.event.PasswordResetRequested;
import com.rally.notification.messaging.event.UserRegistered;
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
public class UserEventsConsumer {
    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.USER, groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, Object> record) {
        String eventType = extractHeader(record, KafkaTopics.HEADER_EVENT_TYPE);
        String messageId = extractHeader(record, KafkaTopics.HEADER_EVENT_ID);
        String correlationId = extractHeader(record, KafkaTopics.HEADER_CORRELATION_ID);

        if (eventType == null) {
            log.warn("Ignoring user event without event type header: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            return;
        }

        log.info("Kafka user event consumed: messageId={}, type={}, correlationId={}, topic={}, partition={}, offset={}",
                messageId, eventType, correlationId, record.topic(), record.partition(), record.offset());

        try {
            switch (eventType) {
                case EventTypes.USER_REGISTERED -> {
                    log.debug("Dispatching {} to notificationService.notifyUserRegistered", eventType);
                    notificationService.notifyUserRegistered((UserRegistered) record.value());
                }
                case EventTypes.USER_EMAIL_VERIFICATION_REQUESTED -> {
                    log.debug("Dispatching {} to notificationService.notifyEmailVerificationRequested", eventType);
                    notificationService.notifyEmailVerificationRequested((EmailVerificationRequested) record.value());
                }
                case EventTypes.USER_PASSWORD_RESET_REQUESTED -> {
                    log.debug("Dispatching {} to notificationService.notifyPasswordResetRequested", eventType);
                    notificationService.notifyPasswordResetRequested((PasswordResetRequested) record.value());
                }
                default -> log.warn("Unhandled user event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process user event: messageId={}, type={}, correlationId={}, topic={}, partition={}, offset={}",
                    messageId, eventType, correlationId, record.topic(), record.partition(), record.offset(), e);
            throw e;
        }

        log.info("Kafka user event processed: messageId={}, type={}, correlationId={}, topic={}",
                messageId, eventType, correlationId, record.topic());
    }

    private String extractHeader(ConsumerRecord<String, Object> record, String key) {
        Header header = record.headers().lastHeader(key);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}