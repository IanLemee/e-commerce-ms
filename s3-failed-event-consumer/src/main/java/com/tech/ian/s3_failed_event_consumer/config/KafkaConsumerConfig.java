package com.tech.ian.s3_failed_event_consumer.config;

import com.tech.ian.s3_failed_event_consumer.config.dto.S3UploadFailDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> getBaseConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "s3-failed-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return config;
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, ?> kafkaTemplate) {
        FixedBackOff backOff = new FixedBackOff(200L, 4L);
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public ConsumerFactory<String, S3UploadFailDto> consumerFactory() {
        JsonDeserializer<S3UploadFailDto> val = new JsonDeserializer<>(S3UploadFailDto.class);
        val.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(getBaseConfig(), new StringDeserializer(), val);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, S3UploadFailDto> kafkaListenerContainerFactory(ConsumerFactory<String, S3UploadFailDto> consumerFactory,
                                                                                                          DefaultErrorHandler defaultErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, S3UploadFailDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler);
        return factory;
    }
}
