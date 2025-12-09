package com.tech.ian.user.config.kafka;

import com.tech.ian.user.config.dto.S3UploadFailEvent;
import com.tech.ian.user.config.dto.S3UploadSuccessEvent;
import com.tech.ian.user.config.dto.UserEmailVerificationDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    private Map<String, Object> getBaseKafkaConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);
        config.put(ProducerConfig.RETRIES_CONFIG, 2);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    @Bean
    public ProducerFactory<String, UserEmailVerificationDto> producerFactoryVerification() {
        Map<String, Object> config = getBaseKafkaConfig();
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, UserEmailVerificationDto> kafkaTemplateVerification() {
        return new KafkaTemplate<>(producerFactoryVerification());
    }

    @Bean
    public ProducerFactory<String, S3UploadFailEvent> producerFactoryS3FailEvent() {
        Map<String, Object> config = getBaseKafkaConfig();
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, S3UploadFailEvent> kafkaTemplateS3FailEvent() {
        return new KafkaTemplate<>(producerFactoryS3FailEvent());
    }

    @Bean
    public ProducerFactory<String, S3UploadSuccessEvent> producerFactoryS3SuccessEvent() {
        Map<String, Object> config = getBaseKafkaConfig();
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, S3UploadSuccessEvent> kafkaTemplateS3SuccessEvent() {
        return new KafkaTemplate<>(producerFactoryS3SuccessEvent());
    }
}
