package com.event.driven.payment.service.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.OrderPaymentEvent;
import com.event.driven.common.service.events.PaymentCompletedEvent;
import com.event.driven.common.service.events.PaymentFailedEvent;
import com.event.driven.common.service.events.PaymentRefundedEvent;
import com.event.driven.payment.service.dto.response.PaymentRefundResponse;
import com.event.driven.payment.service.dto.response.PaymentResponse;
import com.event.driven.payment.service.dto.response.RefundResponse;
import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.enums.EventType;
import com.event.driven.payment.service.enums.TransactionStatus;
import com.event.driven.payment.service.enums.PaymentStatus;
import com.event.driven.payment.service.exception.PaymentNotFoundException;
import com.event.driven.payment.service.repository.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionService paymentTransactionService;
    private final RefundService refundService;
    private final OutboxEventService outboxEventService;
    
    @Autowired
    public PaymentService(PaymentRepository paymentRepository, 
                            PaymentTransactionService paymentTransactionService,
                            RefundService refundService, 
                            OutboxEventService outboxEventService) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionService = paymentTransactionService;
        this.refundService = refundService;
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

        paymentTransactionService.saveTransaction(savedPayment, TransactionStatus.INITIATED);

        Payment updatedPayment;
        if (success()) {
            savedPayment.setPaymentStatus(PaymentStatus.COMPLETED);
            savedPayment.setTransactionId(generateTransactionId());
            updatedPayment = paymentRepository.save(savedPayment);

            paymentTransactionService.saveTransaction(updatedPayment, TransactionStatus.COMPLETED);

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
        } else {
            savedPayment.setPaymentStatus(PaymentStatus.FAILED);
            updatedPayment = paymentRepository.save(savedPayment);

            paymentTransactionService.saveTransaction(updatedPayment, TransactionStatus.FAILED);

            PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                    .paymentId(updatedPayment.getId())
                    .orderId(updatedPayment.getOrderId())
                    .paymentReference(updatedPayment.getPaymentReference())
                    .build();
            outboxEventService.saveEvent(EventType.PAYMENT_FAILED, 
                    AggregateType.PAYMENT, 
                    updatedPayment.getId().toString(), 
                    paymentFailedEvent);
        }
        return toResponse(updatedPayment);
    }

    public PaymentRefundResponse refundPayment(Long id) {
        Payment payment = findPayment(id);
        RefundResponse refundResponse = refundService.processRefund(payment);
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);
        paymentTransactionService.saveTransaction(savedPayment, TransactionStatus.REFUNDED);
        
        PaymentRefundedEvent paymentRefundedEvent = PaymentRefundedEvent.builder()
                                .paymentId(savedPayment.getId())
                                .orderId(savedPayment.getOrderId())
                                .refundId(refundResponse.getId())
                                .amount(savedPayment.getAmount())
                                .build();
        outboxEventService.saveEvent(EventType.PAYMENT_REFUNDED,
                                 AggregateType.PAYMENT,
                                id.toString(), 
                                paymentRefundedEvent);
        PaymentRefundResponse paymentRefundResponse = PaymentRefundResponse.builder()
                                .id(id)
                                .orderId(savedPayment.getOrderId())
                                .customerId(savedPayment.getCustomerId())
                                .amount(savedPayment.getAmount())
                                .refundId(refundResponse.getId())
                                .refundStatus(refundResponse.getRefundStatus())
                                .build();
        return paymentRefundResponse;
    }

    private Payment findPayment(Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(
                    "Payment not found"));   
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

    private boolean success() {
        return Math.random() < 0.9;
    }
}
