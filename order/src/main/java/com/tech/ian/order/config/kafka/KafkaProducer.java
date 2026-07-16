package com.tech.ian.order.config.kafka;

import com.tech.ian.order.config.kafka.dto.OrderBuyProductEventDto;
import com.tech.ian.order.config.kafka.dto.OrderNotificationEventDto;
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
public class KafkaProducer {
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
    public ProducerFactory<String, OrderNotificationEventDto> factoryNotification() {
        return new DefaultKafkaProducerFactory<>(getBaseKafkaConfig());
    }

    @Bean
    public KafkaTemplate<String, OrderNotificationEventDto> kafkaTemplateNotification() {
        return new KafkaTemplate<>(factoryNotification());
    }

    @Bean
    public ProducerFactory<String, OrderBuyProductEventDto> factoryBuyProduct() {
        return new DefaultKafkaProducerFactory<>(getBaseKafkaConfig());
    }

    @Bean
    public KafkaTemplate<String, OrderBuyProductEventDto> kafkaTemplateBuyProduct() {
        return new KafkaTemplate<>(factoryBuyProduct());
    }
}
