package com.tech.ian.stock.config.kafka;

import com.tech.ian.stock.config.kafka.dto.StockBuyProductDto;
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
public class KafkaConsumer {

    public Map<String, Object> consumerDefaultConfig() {
        HashMap<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "stock-service-group");
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.tech.ian.stock.config.kafka.dto.StockBuyProductDto");

        return config;
    }

    @Bean
    public ConsumerFactory<String, StockBuyProductDto> factoryBuyProduct() {
        return new DefaultKafkaConsumerFactory<>(consumerDefaultConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockBuyProductDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StockBuyProductDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(factoryBuyProduct());
        return factory;
    }
}
