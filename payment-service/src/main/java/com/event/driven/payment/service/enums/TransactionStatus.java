package com.event.driven.payment.service.enums;

public enum TransactionStatus {
    INITIATED,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    RETRIED
}
