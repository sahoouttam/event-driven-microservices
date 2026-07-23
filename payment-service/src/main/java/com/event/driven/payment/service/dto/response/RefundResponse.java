package com.event.driven.payment.service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.event.driven.payment.service.enums.RefundStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    
    private Long id;

    private BigDecimal amount;

    private String transactionId;

    private RefundStatus refundStatus;

    private LocalDateTime processedAt;
}
