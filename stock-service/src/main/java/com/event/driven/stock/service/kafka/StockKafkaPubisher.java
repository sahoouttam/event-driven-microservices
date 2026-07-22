package com.event.driven.stock.service.kafka;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.stock.service.entity.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockKafkaPubisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(OutboxEvent outboxEvent) {
        try {
            String topic = resolveTopic(outboxEvent.getAggregateType());
            EventEnvelope eventEnvelope = buildEventEnvelope(outboxEvent);
            kafkaTemplate.send(topic, 
                    outboxEvent.getAggregateId(), 
                    objectMapper.writeValueAsString(eventEnvelope));
            log.info("Published event {} to topic {}", 
                            outboxEvent.getEventType(), topic);
        } catch (Exception ex) {
            throw new IllegalStateException(
                String.format("Failed to publish event. eventId=%s",
                            outboxEvent.getEventId()), ex);
        }
    }

    private String resolveTopic(AggregateType aggregateType) {
        return switch (aggregateType) {
            case AggregateType.PRODUCT -> KafkaTopics.PRODUCT_EVENTS;
            case AggregateType.INVENTORY -> KafkaTopics.INVENTORY_EVENTS;
            default -> throw new IllegalArgumentException(
                    "Unsupported aggregate type: " + aggregateType);
        };
    }

    private EventEnvelope buildEventEnvelope(OutboxEvent outboxEvent) {
        return EventEnvelope.builder()
                    .eventId(outboxEvent.getEventId())
                    .eventType(outboxEvent.getEventType().toString())
                    .aggregateId(outboxEvent.getAggregateId())
                    .aggregateType(outboxEvent.getAggregateType().toString())
                    .timeStamp(LocalDateTime.now())
                    .payload(outboxEvent.getPayload())
                    .build();
                    
    }

}
