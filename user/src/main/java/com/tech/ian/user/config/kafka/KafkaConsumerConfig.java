package com.tech.ian.user.config.kafka;

import com.tech.ian.user.config.dto.S3UploadSuccessEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    private Map<String, Object> getBaseKafkaConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service-group");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.tech.ian.user.config.dto.S3UploadSuccessEvent");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return config;
    }

    @Bean
    public ConsumerFactory<String, S3UploadSuccessEvent> consumerFactory() {
        Map<String, Object> config = getBaseKafkaConfig();
        JsonDeserializer<S3UploadSuccessEvent> value = new JsonDeserializer<>(S3UploadSuccessEvent.class);
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), value);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, S3UploadSuccessEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, S3UploadSuccessEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
