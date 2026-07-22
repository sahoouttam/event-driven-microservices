package com.event.driven.payment.service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.OrderPaymentEvent;
import com.event.driven.common.service.events.PaymentCompletedEvent;
import com.event.driven.payment.service.dto.response.PaymentResponse;
import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.enums.EventType;
import com.event.driven.payment.service.enums.PaymentStatus;
import com.event.driven.payment.service.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OutboxEventService outboxEventService;
    
    public PaymentService(PaymentRepository paymentRepository, OutboxEventService outboxEventService) {
        this.paymentRepository = paymentRepository;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public PaymentResponse createPayment(OrderPaymentEvent orderPaymentEvent) {
        log.info("Processing payment for order {}", orderPaymentEvent.getOrderId());

        Payment payment = Payment.builder()
                    .paymentReference(generatePaymentReference())
                    .orderId(orderPaymentEvent.getOrderId())
                    .customerId(orderPaymentEvent.getCustomerId())
                    .amount(orderPaymentEvent.getTotalAmount())
                    //.paymentMethod(paymentRequest.getPaymentMethod())
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
        Payment savedPayment = paymentRepository.save(payment);
        String transactionId = generateTransactionId();
        
        savedPayment.setPaymentStatus(PaymentStatus.COMPLETED);
        savedPayment.setTransactionId(transactionId);
        Payment updatedPayment = paymentRepository.save(savedPayment);

        PaymentCompletedEvent paymentCompletedEvent = PaymentCompletedEvent.builder()
                    .paymentId(updatedPayment.getId())
                    .orderId(updatedPayment.getOrderId())
                    .paymentReference(updatedPayment.getPaymentReference())
                    .transactionId(updatedPayment.getTransactionId())
                    .build();
        outboxEventService.saveEvent(EventType.PAYMENT_COMPLETED, 
                    AggregateType.PAYMENT, 
                    updatedPayment.getId().toString(), 
                    paymentCompletedEvent);
        
        return toResponse(updatedPayment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                    .id(payment.getId())
                    .paymentReference(payment.getPaymentReference())
                    .orderId(payment.getOrderId())
                    .customerId(payment.getCustomerId())
                    .amount(payment.getAmount())
                    .paymentStatus(payment.getPaymentStatus())
                    .build();
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString()
                    .substring(0, 8).toUpperCase();
    }

    private String generateTransactionId() {
        return "TRANSACTION-" + UUID.randomUUID().toString()
                    .substring(0, 8).toUpperCase();
    }
}
