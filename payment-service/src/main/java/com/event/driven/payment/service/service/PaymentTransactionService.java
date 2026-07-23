package com.event.driven.payment.service.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.payment.service.dto.response.PaymentTransactionResponse;
import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.entity.PaymentTransaction;
import com.event.driven.payment.service.enums.TransactionStatus;
import com.event.driven.payment.service.repository.PaymentTransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentTransactionService {
    
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    public void saveTransaction(Payment payment, TransactionStatus transactionStatus) {
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                        .payment(payment)
                        .paymentAction(transactionStatus)
                        .transactionReference(generateTransactionReference())
                        .pspReference(generatePSPReference())
                        .build();
        paymentTransactionRepository.save(paymentTransaction);
        log.info("saved transaction {} for {} payment", 
                                        paymentTransaction.getId(),
                                        payment.getId());
    }

    public List<PaymentTransactionResponse> getTransactionHistory(Payment payment) {
        return paymentTransactionRepository.findByPayment(payment)
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
    }

    private PaymentTransactionResponse mapToResponse(PaymentTransaction paymentTransaction) {
        return PaymentTransactionResponse.builder()
                        .id(paymentTransaction.getId())
                        .paymentAction(paymentTransaction.getPaymentAction())
                        .transactionReference(paymentTransaction.getTransactionReference())
                        .pspReference(paymentTransaction.getPspReference())
                        .build();
    }

    private String generatePSPReference() {
        return "PSP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateTransactionReference() {
        return "TXR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
