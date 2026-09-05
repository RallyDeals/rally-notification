package com.rally.notification.messaging.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }
    public static final String ORDER = "order.lifecycle_events";
    public static final String USER = "user.events";

    public static final String HEADER_EVENT_ID = "X-Id";
    public static final String HEADER_EVENT_TYPE = "X-Type";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_TRACEPARENT = "traceparent";
}