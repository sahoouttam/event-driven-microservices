package com.event.driven.payment.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.events.OrderPaymentEvent;
import com.event.driven.common.service.exceptions.EventSerializationException;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.payment.service.dto.response.PaymentResponse;
import com.event.driven.payment.service.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentKafkaConsumer {
    
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @Autowired
    public PaymentKafkaConsumer(PaymentService paymentService, 
                                ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = KafkaTopics.ORDER_EVENTS,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeEvent(String envelopeJson) {
        try {
            EventEnvelope eventEnvelope = objectMapper.readValue(
                                                envelopeJson, EventEnvelope.class);
            log.info("Received event type: {}, eventId: {}", 
                    eventEnvelope.getEventType(), eventEnvelope.getEventId());
            String eventType = eventEnvelope.getEventType();  
            if ("ORDER_PAYMENT_INITIATED".equals(eventType)) {
                OrderPaymentEvent orderPaymentEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), OrderPaymentEvent.class);
                PaymentResponse paymentResponse = paymentService
                                        .createPayment(orderPaymentEvent);
                log.info("Payment made successfully for order {}, amount {}",
                            paymentResponse.getOrderId(), 
                            paymentResponse.getAmount());
            }

        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Unable to serialize event", ex);
        }
    }
}
