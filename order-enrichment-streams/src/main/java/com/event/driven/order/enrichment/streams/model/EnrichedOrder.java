package com.event.driven.order.enrichment.streams.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedOrder {
    
    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderCreateAt;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;

    private boolean stockReserved;

    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paymentProcessedAt;

    private String fulfillmentStatus;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private List<EnrichedOrderItem> enrichedOrderItems;
}
