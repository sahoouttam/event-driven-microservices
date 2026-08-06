package com.event.driven.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.OrderPaymentEvent;
import com.event.driven.payment.service.dto.response.PaymentResponse;
import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.enums.PaymentStatus;
import com.event.driven.payment.service.exception.PaymentNotFoundException;
import com.event.driven.payment.service.repository.PaymentRepository;
import com.event.driven.payment.service.service.OutboxEventService;
import com.event.driven.payment.service.service.PaymentService;
import com.event.driven.payment.service.service.PaymentTransactionService;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() {
        OrderPaymentEvent orderPaymentEvent = OrderPaymentEvent.builder()
                                .orderId(1L)
                                .customerId(2L)
                                .totalAmount(BigDecimal.valueOf(100))
                                .paymentMethod("CREDIT_CARD")
                                .build();
        
        Payment savedPayment = Payment.builder()
                                .id(3L)
                                .paymentReference("PAY_ABC")
                                .orderId(1L)
                                .customerId(2L)
                                .amount(BigDecimal.valueOf(100))
                                .paymentStatus(PaymentStatus.PENDING)
                                .build();

        Payment completedPayment = Payment.builder()
                                .id(3L)
                                .paymentReference("PAY_ABC")
                                .orderId(1L)
                                .customerId(2L)
                                .amount(BigDecimal.valueOf(100))
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .transactionId("TXN-ABC")
                                .build();

        when(paymentRepository.save(any(Payment.class)))
                                .thenReturn(savedPayment)
                                .thenReturn(completedPayment);

        PaymentResponse paymentResponse = paymentService.createPayment(orderPaymentEvent);

        assertNotNull(paymentResponse);
        assertEquals(1L, paymentResponse.getOrderId());
        verify(paymentTransactionService, times(2)).saveTransaction(any(), any());
        verify(outboxEventService).saveEvent(any(), 
                                eq(AggregateType.PAYMENT), anyString(), any());

    }

    @Test
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.refundPayment(999L);
        });
    }
}
