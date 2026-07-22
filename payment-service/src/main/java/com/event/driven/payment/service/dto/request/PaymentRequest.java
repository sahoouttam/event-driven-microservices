package com.event.driven.payment.service.dto.request;

import java.math.BigDecimal;

import com.event.driven.payment.service.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    private Long orderId;

    private Long customerId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;
}
