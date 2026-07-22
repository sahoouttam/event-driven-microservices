package com.event.driven.common.service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    
    private Long paymentId;

    private Long orderId;

    private String paymentReference;

    private String transactionId;
}
