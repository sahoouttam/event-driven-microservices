package com.event.driven.payment.service.dto.response;

import java.math.BigDecimal;

import com.event.driven.payment.service.enums.RefundStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundResponse {
    
    private Long id;

    private Long orderId;

    private Long customerId;

    private BigDecimal amount;

    private Long refundId;

    private RefundStatus refundStatus;
}
