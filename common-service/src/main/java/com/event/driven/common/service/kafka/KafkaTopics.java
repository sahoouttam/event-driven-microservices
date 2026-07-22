package com.event.driven.common.service.kafka;

public class KafkaTopics {

    private KafkaTopics() { 

    } 
    public static final String PRODUCT_EVENTS = "product-events"; 
    
    public static final String INVENTORY_EVENTS = "inventory-events";

    public static final String ORDER_EVENTS = "order-events";

    public static final String PAYMENT_EVENTS = "payment-events";
}
