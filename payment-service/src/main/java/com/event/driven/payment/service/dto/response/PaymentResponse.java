package com.event.driven.payment.service.dto.response;

import java.math.BigDecimal;

import com.event.driven.payment.service.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;

    private String paymentReference;

    private Long orderId;

    private Long customerId;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;
}
