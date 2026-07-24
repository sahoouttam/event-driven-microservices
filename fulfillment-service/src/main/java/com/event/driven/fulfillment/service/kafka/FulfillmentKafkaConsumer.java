package com.event.driven.fulfillment.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.events.OrderCancelledEvent;
import com.event.driven.common.service.events.OrderConfirmedEvent;
import com.event.driven.common.service.exceptions.EventSerializationException;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.fulfillment.service.service.FulfillmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FulfillmentKafkaConsumer {
    
    private final FulfillmentService fulfillmentService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FulfillmentKafkaConsumer(FulfillmentService fulfillmentService, 
                                    ObjectMapper objectMapper) {
        this.fulfillmentService = fulfillmentService;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(
        topics = KafkaTopics.ORDER_EVENTS,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderEvent(String envelopeJson) {
        try {
            EventEnvelope eventEnvelope = objectMapper.readValue(
                                                envelopeJson, EventEnvelope.class);
            log.info("Received event type: {}, eventId: {}", 
                    eventEnvelope.getEventType(), eventEnvelope.getEventId());  
            switch (eventEnvelope.getEventType()) {
                case "ORDER_CONFIRMED" -> {
                    OrderConfirmedEvent orderConfirmedEvent = objectMapper.readValue(
                            eventEnvelope.getPayload(), OrderConfirmedEvent.class);
                    fulfillmentService.createFulfillment(orderConfirmedEvent);
                }
                case "ORDER_CANCELLED" -> {
                    OrderCancelledEvent orderCancelledEvent = objectMapper.readValue(
                            eventEnvelope.getPayload(), OrderCancelledEvent.class);
                }      
            }

        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Unable to serialize event", ex);
        }
    }

}
