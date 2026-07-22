package com.event.driven.payment.service.enums;

public enum PaymentAction {
    INITIATED,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    RETRIED
}
