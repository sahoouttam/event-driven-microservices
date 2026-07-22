package com.event.driven.order.service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.order.service.client.StockClient;
import com.event.driven.order.service.dto.request.OrderItemRequest;
import com.event.driven.order.service.dto.response.ProductResponse;
import com.event.driven.order.service.entity.Order;
import com.event.driven.order.service.entity.OrderItem;
import com.event.driven.order.service.repository.OrderItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    private final StockClient stockClient;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository, StockClient stockClient) {
        this.orderItemRepository = orderItemRepository;
        this.stockClient = stockClient;
    }

    public OrderItem createOrderItem(OrderItemRequest orderItemRequest, Order order) {
        ProductResponse productResponse = stockClient
                                                .getProductBySku(orderItemRequest.getSku());

        OrderItem orderItem = OrderItem.builder()
                        .productId(productResponse.getId())
                        .sku(productResponse.getSku())
                        .productName(productResponse.getName())
                        .order(order)
                        .unitPrice(productResponse.getPrice())
                        .quantity(orderItemRequest.getQuantity())
                        .subTotal(productResponse.getPrice().multiply(
                            BigDecimal.valueOf(orderItemRequest.getQuantity())))
                        .build();
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        return savedOrderItem;
        
    }

    public List<OrderItem> findAllOrderItems(Order order) {
        return orderItemRepository.findByOrder(order);
    }
}
