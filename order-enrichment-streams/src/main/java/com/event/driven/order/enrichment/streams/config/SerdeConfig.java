package com.event.driven.order.enrichment.streams.config;

import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.order.enrichment.streams.model.EnrichedOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class SerdeConfig {
    
    private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

    @Bean
    public Serde<EventEnvelope> evenEnvelopeSerde() {
        return new JsonSerde<>(EventEnvelope.class, objectMapper);
    }

    @Bean
    public Serde<EnrichedOrder> enrichedOrderSerde() {
        return new JsonSerde<>(EnrichedOrder.class, objectMapper);
    }
}
