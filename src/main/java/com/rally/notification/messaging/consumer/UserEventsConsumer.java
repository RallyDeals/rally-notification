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
        Object event = record.value();
        String eventType = extractType(record);
        if (eventType == null) return;
        switch (eventType) {
            case EventTypes.USER_REGISTERED ->
                    notificationService.notifyUserRegistered((UserRegistered) event);
            case EventTypes.USER_EMAIL_VERIFICATION_REQUESTED ->
                    notificationService.notifyEmailVerificationRequested((EmailVerificationRequested) event);
            case EventTypes.USER_PASSWORD_RESET_REQUESTED ->
                    notificationService.notifyPasswordResetRequested((PasswordResetRequested) event);
            default -> log.warn("Unhandled user event type: {}", eventType);
        }
    }

    private String extractType(ConsumerRecord<String, Object> record) {
        Header header = record.headers().lastHeader(KafkaTopics.HEADER_EVENT_TYPE);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
