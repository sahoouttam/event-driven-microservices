package com.event.driven.payment.service.kafka;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.payment.service.entity.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentKafkaPubisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(OutboxEvent outboxEvent) {
        try {
            EventEnvelope eventEnvelope = buildEventEnvelope(outboxEvent);
            kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, 
                    outboxEvent.getAggregateId(), 
                    objectMapper.writeValueAsString(eventEnvelope));
            log.info("Published event {} to topic {}", 
                            outboxEvent.getEventType(), 
                            KafkaTopics.PAYMENT_EVENTS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                String.format("Failed to publish event. eventId=%s",
                            outboxEvent.getEventId()), ex);
        }
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
