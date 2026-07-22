package com.event.driven.common.service.events;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
    
    private Long orderId;

    private Long customerId;

    private List<OrderItemEvent> orderItemEvents;
}
