package com.event.driven.order.enrichment.streams.processor;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import com.event.driven.common.service.events.EventEnvelope;
import com.event.driven.common.service.events.FulfillmentDeliveredEvent;
import com.event.driven.common.service.events.FulfillmentShippedEvent;
import com.event.driven.common.service.events.OrderCreatedEvent;
import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.common.service.events.PaymentCompletedEvent;
import com.event.driven.common.service.events.PaymentFailedEvent;
import com.event.driven.common.service.events.ReturnCompletedEvent;
import com.event.driven.order.enrichment.streams.client.AccountClient;
import com.event.driven.order.enrichment.streams.dto.CustomerResponse;
import com.event.driven.order.enrichment.streams.model.EnrichedOrder;
import com.event.driven.order.enrichment.streams.model.EnrichedOrderItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderEnrichmentProcessor implements 
        Transformer<String, EventEnvelope, KeyValue<String, EnrichedOrder>> {

    private final AccountClient accountClient;
    private final ObjectMapper objectMapper;
    private KeyValueStore<String, EnrichedOrder> keyValueStore;
    private ProcessorContext processorContext;

    public OrderEnrichmentProcessor(AccountClient accountClient, ObjectMapper objectMapper) {
        this.accountClient = accountClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init(ProcessorContext processorContext) {
        this.processorContext = processorContext;
        this.keyValueStore = processorContext.getStateStore("enriched-orders-store");

        processorContext.schedule(Duration.ofMinutes(1), PunctuationType.WALL_CLOCK_TIME, timestamp ->{
            try (KeyValueIterator<String, EnrichedOrder> keyValueIterator = keyValueStore.all()) {
                while (keyValueIterator.hasNext()) {
                    KeyValue<String, EnrichedOrder> entry = keyValueIterator.next();
                    EnrichedOrder enrichedOrder = entry.value;
                    if (isOrderComplete(enrichedOrder)) {
                        processorContext.forward(entry.key, enrichedOrder);
                        log.info("Forwarded enriched order: {}", enrichedOrder.getOrderId());
                    }
                }
            }
        });
    }

    @Override
    public KeyValue<String, EnrichedOrder> transform(String key, EventEnvelope eventEnvelope) {
        String orderId = extractOrderId(eventEnvelope);
        if (orderId == null) return null;

        EnrichedOrder enrichedOrder = keyValueStore.get(orderId);
        if (enrichedOrder == null) {
            enrichedOrder = new EnrichedOrder();
            enrichedOrder.setOrderNumber(orderId);
        }

        updateEnrichedOrder(enrichedOrder, eventEnvelope);
        keyValueStore.put(orderId, enrichedOrder);

        log.info("Enriched order {} with event: {}", orderId, eventEnvelope.getEventType());
        return null;
    }

    private void updateEnrichedOrder(EnrichedOrder enrichedOrder, EventEnvelope eventEnvelope) {
        try {
            switch (eventEnvelope.getEventType()) {
                case "ORDER_CREATED" -> {
                    OrderCreatedEvent orderCreatedEvent = objectMapper
                            .readValue(eventEnvelope.getPayload(), OrderCreatedEvent.class);
                    enrichedOrder.setOrderNumber(orderCreatedEvent.getOrderNumber());
                    enrichedOrder.setTotalAmount(orderCreatedEvent.getTotalAmount());
                    enrichedOrder.setOrderStatus("PENDING_INVENTORY");
                    enrichedOrder.setEnrichedOrderItems(
                            mapOrderItems(orderCreatedEvent.getOrderItemEvents()));
                    enrichCustomerDetails(enrichedOrder);
                }
                case "STOCK_RESERVED" -> {
                    enrichedOrder.setStockReserved(true);
                    enrichedOrder.setOrderStatus("PENDING_PAYMENT");
                }
                case "STOCK_RESERVED_FAILED" -> {
                    enrichedOrder.setOrderStatus("ORDER_FAILED");
                }
                case "PAYMENT_COMPLETED" -> {
                    PaymentCompletedEvent paymentCompletedEvent = objectMapper
                            .readValue(eventEnvelope.getPayload(), PaymentCompletedEvent.class);
                    enrichedOrder.setPaymentStatus("PAYMENT_COMPLETED");
                    enrichedOrder.setTransactionId(paymentCompletedEvent.getTransactionId());
                    enrichedOrder.setOrderStatus("ORDER_CONFIRMED");
                }
                case "PAYMENT_FAILED" -> {
                    PaymentFailedEvent paymentFailedEvent = objectMapper
                            .readValue(eventEnvelope.getPayload(), PaymentFailedEvent.class);
                    enrichedOrder.setPaymentStatus("PAYMENT_FAILED");
                    enrichedOrder.setOrderStatus("ORDER_CANCELLED");
                }
                case "FULFILLMENT_SHIPPED" -> {
                    FulfillmentShippedEvent fulfillmentShippedEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), FulfillmentShippedEvent.class);
                    enrichedOrder.setFulfillmentStatus("SHIPPED");
                    enrichedOrder.setTrackingNumber(fulfillmentShippedEvent.getTrackingNumber());
                    enrichedOrder.setCarrier(fulfillmentShippedEvent.getCarrier());
                    enrichedOrder.setOrderStatus("ORDER_SHIPPED");
                }
                case "FULFILLMENT_DELIVERED" -> {
                    FulfillmentDeliveredEvent fulfillmentDeliveredEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), FulfillmentDeliveredEvent.class);
                    enrichedOrder.setFulfillmentStatus("DELIVERED");
                    enrichedOrder.setOrderStatus("ORDER_DELIVERED");
                }
                 case "RETURN_COMPLETED" -> {
                    ReturnCompletedEvent returnCompletedEvent = objectMapper
                        .readValue(eventEnvelope.getPayload(), ReturnCompletedEvent.class);
                    enrichedOrder.setOrderStatus("ORDER_RETURNED");
                }
            }
        } catch (Exception e) {
            log.error("Failed to update enriched order: {}", enrichedOrder.getOrderId(), e);
        }
    }

    private void enrichCustomerDetails(EnrichedOrder enrichedOrder) {
        if (enrichedOrder.getCustomerId() != null) {
            Long customerId = enrichedOrder.getCustomerId();
            CustomerResponse customerResponse = accountClient.getCustomer(customerId);
            enrichedOrder.setCustomerId(customerResponse.getId());
            enrichedOrder.setCustomerEmail(customerResponse.getEmail());
            enrichedOrder.setCustomerPhone(customerResponse.getPhone());
        }
    }

    private List<EnrichedOrderItem> mapOrderItems(List<OrderItemEvent> orderItemEvents) {
        return orderItemEvents.stream()
                    .map(this::mapOrderItem)
                    .collect(Collectors.toList());
    }

    private EnrichedOrderItem mapOrderItem(OrderItemEvent orderItemEvent) {
        return EnrichedOrderItem.builder()
                    .productId(orderItemEvent.getProductId())
                    .sku(orderItemEvent.getSku())
                    .productName(orderItemEvent.getProductName())
                    .quantity(orderItemEvent.getQuantity())
                    .stockAvailable(false)
                    .build();
    }

    private String extractOrderId(EventEnvelope eventEnvelope) {
        try {
            var node = objectMapper.readTree(eventEnvelope.getPayload());
            if (node.has("orderId")) return node.get("orderId").asText();
            if (node.has("order_id")) return node.get("order_id").asText();
        } catch (Exception e) {
            
        }
        return eventEnvelope.getAggregateId();
    }

    private boolean isOrderComplete(EnrichedOrder order) {
        return order.getOrderNumber() != null && order.getOrderStatus() != null;
    }

    @Override
    public void close() {
        
    }
}
