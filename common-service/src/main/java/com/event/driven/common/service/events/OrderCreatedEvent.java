package com.event.driven.common.service.events;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    
    private Long orderId;

    private String orderNumber;

    private Long customerId;

    private BigDecimal totalAmount;

    private List<OrderItemEvent> orderItemEvents;
}
