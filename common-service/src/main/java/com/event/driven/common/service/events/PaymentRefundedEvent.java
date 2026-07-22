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
public class PaymentRefundedEvent {
    
    private Long paymentId;

    private Long orderId;

    private Long refundId;

    private BigDecimal amount;

}
