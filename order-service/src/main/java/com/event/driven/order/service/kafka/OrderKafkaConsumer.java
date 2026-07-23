package com.event.driven.order.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.events.PaymentCompletedEvent;
import com.event.driven.common.service.events.PaymentFailedEvent;
import com.event.driven.common.service.events.PaymentRefundedEvent;
import com.event.driven.common.service.events.StockReservationEvent;
import com.event.driven.common.service.events.StockReservationFailedEvent;
import com.event.driven.common.service.exceptions.EventSerializationException;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.order.service.enums.OrderStatus;
import com.event.driven.order.service.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderKafkaConsumer {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderKafkaConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(
        topics = KafkaTopics.INVENTORY_EVENTS,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeInventoryEvent(String envelopeJson) {
        try {
            EventEnvelope eventEnvelope = objectMapper.readValue(
                                                envelopeJson, EventEnvelope.class);
            log.info("Received event type: {}, eventId: {}", 
                    eventEnvelope.getEventType(), eventEnvelope.getEventId());  
            switch (eventEnvelope.getEventType()) {
                case "STOCK_RESERVATION_UPDATED" -> {
                    StockReservationEvent reservationEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), 
                                    StockReservationEvent.class);
                    orderService.updateStatus(reservationEvent.getOrderId(), OrderStatus.PENDING_PAYMENT);
                }
                case "STOCK_RESERVATION_FAILED" -> {
                    StockReservationFailedEvent failedEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), 
                                    StockReservationFailedEvent.class);
                    orderService.updateStatus(failedEvent.getOrderId(), OrderStatus.FAILED);
                }      
            }

        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Unable to serialize event", ex);
        }
    }

    @KafkaListener(
        topics = KafkaTopics.PAYMENT_EVENTS,
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePaymentEvent(String envelopeJson) {
        try {
            EventEnvelope eventEnvelope = objectMapper.readValue(
                                                envelopeJson, EventEnvelope.class);
            log.info("Received event type: {}, eventId: {}", 
                    eventEnvelope.getEventType(), eventEnvelope.getEventId());  
            switch (eventEnvelope.getEventType()) {
                case "PAYMENT_COMPLETED" -> {
                    PaymentCompletedEvent paymentCompletedEvent = objectMapper
                            .readValue(eventEnvelope.getPayload(), 
                                        PaymentCompletedEvent.class);
                    orderService.updateStatus(paymentCompletedEvent.getOrderId(), 
                                    OrderStatus.CONFIRMED);
                }
                case "PAYMENT_FAILED" -> {
                    PaymentFailedEvent paymentFailedEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), 
                                    PaymentFailedEvent.class);
                    orderService.updateStatus(paymentFailedEvent.getOrderId(), 
                                        OrderStatus.CANCELLED);
                } 
                case "PAYMENT_REFUNDED" -> {
                    PaymentRefundedEvent paymentRefundedEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), 
                                    PaymentRefundedEvent.class);
                    orderService.updateStatus(paymentRefundedEvent.getOrderId(), 
                                        OrderStatus.PAYMENT_REFUNDED);
                }      
            }

        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Unable to serialize event", ex);
        }
    }
}
