package com.event.driven.order.service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.event.driven.order.service.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;

    private String orderNumber;

    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private List<OrderItemResponse> orderItems;

    private LocalDateTime createdAt;
}
