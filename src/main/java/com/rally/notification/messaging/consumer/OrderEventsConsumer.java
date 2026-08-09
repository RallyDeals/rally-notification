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
        Object event = record.value();
        String eventType = extractType(record);
        if (eventType == null) return;
        switch (eventType) {
            case EventTypes.ORDER_CREATED -> notificationService.notifyOrderCreated((OrderCreated) event);
            case EventTypes.ORDER_AUTHORIZED -> notificationService.notifyOrderAuthorized((OrderAuthorized) event);
            case EventTypes.ORDER_DEAL_CANCELLED ->
                    notificationService.notifyDealOrderCancelled((DealOrderCancelled) event);
            case EventTypes.ORDER_NORMAL_CANCELLED ->
                    notificationService.notifyNormalOrderCancelled((NormalOrderCancelled) event);
            default -> System.out.println("Unhandled participation event type: " + eventType);
        }
    }

    private String extractType(ConsumerRecord<String, Object> record) {
        Header header = record.headers().lastHeader(KafkaTopics.HEADER_EVENT_TYPE);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
