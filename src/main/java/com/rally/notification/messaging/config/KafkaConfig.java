package com.rally.notification.messaging.config;
import com.rally.notification.messaging.event.DealOrderCancelled;
import com.rally.notification.messaging.event.NormalOrderCancelled;
import com.rally.notification.messaging.event.OrderCreated;
import com.rally.notification.messaging.event.OrderAuthorized;
import com.rally.notification.messaging.event.EmailVerificationRequested;
import com.rally.notification.messaging.event.PasswordResetRequested;
import com.rally.notification.messaging.event.UserRegistered;
import com.rally.notification.messaging.support.EventTypes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@EnableScheduling
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public DefaultErrorHandler errorHandler() {
        BackOff backOff = new FixedBackOff(1000L, 3L);
        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);
        handler.addNotRetryableExceptions(DeserializationException.class);
        return handler;
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        DefaultJacksonJavaTypeMapper typeMapper = createTypeMapper();
        JacksonJsonDeserializer<Object> jsonDeserializer = new JacksonJsonDeserializer<>();
        jsonDeserializer.setTypeMapper(typeMapper);
        jsonDeserializer = jsonDeserializer.dontRemoveTypeHeaders();
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer));
    }

    private static @NonNull DefaultJacksonJavaTypeMapper createTypeMapper() {
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setClassIdFieldName("X-Type");
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("com.rally.notification.messaging.event");
        typeMapper.setIdClassMapping(Map.ofEntries(
                Map.entry(EventTypes.ORDER_CREATED, OrderCreated.class),
                Map.entry(EventTypes.ORDER_DEAL_CANCELLED, DealOrderCancelled.class),
                Map.entry(EventTypes.ORDER_NORMAL_CANCELLED, NormalOrderCancelled.class),
                Map.entry(EventTypes.ORDER_AUTHORIZED, OrderAuthorized.class),
                Map.entry(EventTypes.USER_REGISTERED, UserRegistered.class),
                Map.entry(EventTypes.USER_EMAIL_VERIFICATION_REQUESTED, EmailVerificationRequested.class),
                Map.entry(EventTypes.USER_PASSWORD_RESET_REQUESTED, PasswordResetRequested.class)
        ));
        return typeMapper;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}