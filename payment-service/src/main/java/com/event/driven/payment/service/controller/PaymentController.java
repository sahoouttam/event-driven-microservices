package com.event.driven.payment.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.event.driven.payment.service.dto.response.PaymentRefundResponse;
import com.event.driven.payment.service.dto.response.PaymentResponse;
import com.event.driven.payment.service.service.PaymentService;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        PaymentResponse paymentResponse = paymentService.getPayment(paymentId);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(@PathVariable Long orderId) {
        PaymentResponse paymentResponse = paymentService.getPaymentByOrder(orderId);
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentRefundResponse> refundPayment(@PathVariable Long paymentId) {
        PaymentRefundResponse paymentRefundResponse = paymentService.refundPayment(paymentId);
        return new ResponseEntity<>(paymentRefundResponse, HttpStatus.OK);
    }
    
}
