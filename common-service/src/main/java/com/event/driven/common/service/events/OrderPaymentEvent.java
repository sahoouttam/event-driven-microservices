package com.event.driven.common.service.events;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentEvent {
    
    private Long orderId;

    private String orderNumber;

    private Long customerId;

    private BigDecimal totalAmount;
}
