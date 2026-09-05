package com.rally.notification.config.rest;

import com.rally.notification.messaging.config.KafkaTopics;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TracingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String correlationId = MDC.get(KafkaTopics.HEADER_CORRELATION_ID);
        if (correlationId != null && !correlationId.isBlank()) {
            request.getHeaders().set(KafkaTopics.HEADER_CORRELATION_ID, correlationId);
        }

        Span span = tracer.currentSpan();
        if (span != null) {
            TraceContext context = span.context();
            if (context != null && context.traceId() != null && context.spanId() != null) {
                request.getHeaders().set(KafkaTopics.HEADER_TRACEPARENT,
                        "00-" + context.traceId() + "-" + context.spanId() + "-01");
            }
        }

        return execution.execute(request, body);
    }
}