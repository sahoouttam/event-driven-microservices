package com.event.driven.stock.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.events.OrderCancelledEvent;
import com.event.driven.common.service.events.OrderConfirmedEvent;
import com.event.driven.common.service.events.OrderCreatedEvent;
import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.common.service.exceptions.EventSerializationException;
import com.event.driven.common.service.kafka.KafkaTopics;
import com.event.driven.stock.service.dto.request.StockOperationRequest;
import com.event.driven.stock.service.dto.response.InventoryResponse;
import com.event.driven.stock.service.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StockKafkaConsumer {
    
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @Autowired
    public StockKafkaConsumer(InventoryService inventoryService, 
                                ObjectMapper objectMapper) {
        this.inventoryService = inventoryService;
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
            if ("ORDER_CREATED".equals(eventType)) {
                OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(
                                eventEnvelope.getPayload(), 
                                OrderCreatedEvent.class);
                for (OrderItemEvent orderItemEvent : orderCreatedEvent.getOrderItemEvents()) {
                    StockOperationRequest stockOperationRequest = new StockOperationRequest(
                            orderItemEvent.getSku(),
                            orderItemEvent.getQuantity()
                    );
                    InventoryResponse inventoryResponse = inventoryService.updateReservation(stockOperationRequest);
                    log.info("Stock reserved for order item {}, with sku {} and quantity {}",
                                        inventoryResponse.getProductName(),
                                        inventoryResponse.getSku(),
                                        inventoryResponse.getAvailableQuantity());
                }
            } else if ("ORDER_CONFIRMED".equals(eventType)) {
                OrderConfirmedEvent orderConfirmedEvent = objectMapper.readValue(
                    eventEnvelope.getPayload(), OrderConfirmedEvent.class);
                for (OrderItemEvent orderItemEvent : orderConfirmedEvent.getOrderItemEvents()) {
                    StockOperationRequest stockOperationRequest = new StockOperationRequest(
                            orderItemEvent.getSku(),
                            orderItemEvent.getQuantity()
                    );
                    InventoryResponse inventoryResponse = inventoryService.confirmReservation(stockOperationRequest);
                    log.info("Reservation confirmed for order item {}, with sku {} and quantity {}",
                                        inventoryResponse.getProductName(),
                                        inventoryResponse.getSku(),
                                        inventoryResponse.getAvailableQuantity());
                }
            } else if ("ORDER_CANCELLED".equals(eventType)) {
                OrderCancelledEvent orderCancelledEvent = objectMapper.readValue(
                    eventEnvelope.getPayload(), OrderCancelledEvent.class);
                for (OrderItemEvent orderItemEvent : orderCancelledEvent.getOrderItemEvents()) {
                    StockOperationRequest stockOperationRequest = new StockOperationRequest(
                            orderItemEvent.getSku(),
                            orderItemEvent.getQuantity()
                    );
                    InventoryResponse inventoryResponse = inventoryService.releaseReservation(stockOperationRequest);
                    log.info("Reservation cancelled for order item {}, with sku {} and quantity {}",
                                        inventoryResponse.getProductName(),
                                        inventoryResponse.getSku(),
                                        inventoryResponse.getAvailableQuantity());
                }
            }

        } catch (JsonProcessingException ex) {
            throw new EventSerializationException("Unable to serialize event", ex);
        }
    }
}
