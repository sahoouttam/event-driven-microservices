package com.event.driven.payment.service.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.payment.service.dto.response.RefundResponse;
import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.entity.Refund;
import com.event.driven.payment.service.enums.RefundStatus;
import com.event.driven.payment.service.repository.RefundRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RefundService {
    
    private final RefundRepository refundRepository;

    @Autowired
    public RefundService(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }
    
    public RefundResponse processRefund(Payment payment) {
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(payment.getAmount())
                .transactionId(generateTransactionId())
                .refundStatus(RefundStatus.REFUND_COMPLETED)
                .processedAt(LocalDateTime.now())
                .build();
        Refund savedRefund = refundRepository.save(refund);
        return mapToResponse(savedRefund);
    }

    private RefundResponse mapToResponse(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .amount(refund.getAmount())
                .transactionId(refund.getTransactionId())
                .refundStatus(refund.getRefundStatus())
                .processedAt(refund.getProcessedAt())
                .build();
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
}
