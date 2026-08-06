package com.event.driven.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.event.driven.payment.service.dto.response.PaymentTransactionResponse;
import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.entity.PaymentTransaction;
import com.event.driven.payment.service.enums.TransactionStatus;
import com.event.driven.payment.service.repository.PaymentTransactionRepository;
import com.event.driven.payment.service.service.PaymentTransactionService;

@ExtendWith(MockitoExtension.class)
public class PaymentTransactionServiceTest {
    
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    @Test
    void shouldSaveTransaction() {
        Payment payment = Payment.builder()
                            .id(1L)
                            .build();
        paymentTransactionService.saveTransaction(payment, TransactionStatus.INITIATED);

        verify(paymentTransactionRepository).save(any(PaymentTransaction.class));
    }

    @Test
    void shouldGetTransactionHistory() {
        Payment payment = Payment.builder()
                            .id(1L)
                            .build();
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                            .id(1L)
                            .paymentAction(TransactionStatus.INITIATED)
                            .transactionReference("TXR-123")
                            .pspReference("PSP-456")
                            .build();

        when(paymentTransactionRepository.findByPayment(payment))
                            .thenReturn(Arrays.asList(paymentTransaction));

        List<PaymentTransactionResponse> transactionResponses = paymentTransactionService
                                        .getTransactionHistory(payment);
        assertEquals(1, transactionResponses.size());
        assertEquals(TransactionStatus.INITIATED, transactionResponses.get(0).getPaymentAction());
    }
}
