package com.event.driven.order.service.mapper;

import org.springframework.stereotype.Component;

import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.order.service.dto.response.OrderItemResponse;
import com.event.driven.order.service.dto.response.OrderResponse;
import com.event.driven.order.service.entity.Order;
import com.event.driven.order.service.entity.OrderItem;

@Component
public class OrderMapper {
    
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                    .id(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .orderStatus(order.getOrderStatus())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .build();
    }

    public OrderItemResponse toResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                    .id(orderItem.getId())
                    .productName(orderItem.getProductName())
                    .unitPrice(orderItem.getUnitPrice())
                    .quantity(orderItem.getQuantity())
                    .subTotal(orderItem.getSubTotal())
                    .build();
    }

    public OrderItemEvent toEvent(OrderItem orderItem) {
        return OrderItemEvent.builder()
                    .productId(orderItem.getProductId())
                    .sku(orderItem.getSku())
                    .quantity(orderItem.getQuantity())
                    .build();
    }
}
