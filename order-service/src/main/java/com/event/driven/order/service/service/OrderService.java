package com.event.driven.order.service.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.common.service.enums.AggregateType;
import com.event.driven.common.service.events.OrderCancelledEvent;
import com.event.driven.common.service.events.OrderConfirmedEvent;
import com.event.driven.common.service.events.OrderCreatedEvent;
import com.event.driven.common.service.events.OrderItemEvent;
import com.event.driven.common.service.events.OrderPaymentEvent;
import com.event.driven.order.service.dto.request.CreateOrderRequest;
import com.event.driven.order.service.dto.request.OrderItemRequest;
import com.event.driven.order.service.dto.response.OrderResponse;
import com.event.driven.order.service.entity.Order;
import com.event.driven.order.service.entity.OrderItem;
import com.event.driven.order.service.enums.EventType;
import com.event.driven.order.service.enums.OrderStatus;
import com.event.driven.order.service.exception.ResourceNotFoundException;
import com.event.driven.order.service.mapper.OrderMapper;
import com.event.driven.order.service.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final OrderMapper orderMapper;
    private final OutboxEventService outboxEventService;

    @Autowired
    public OrderService(OrderRepository orderRepository, 
                        OrderMapper orderMapper,
                        OrderItemService orderItemService,
                        OutboxEventService outboxEventService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderItemService = orderItemService;
        this.outboxEventService = outboxEventService;
    }

    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerId(createOrderRequest.getCustomerId())
                .orderStatus(OrderStatus.PENDING_INVENTORY)
                .totalAmount(BigDecimal.ZERO)
                .build();
        Order savedOrder = orderRepository.save(order);

        List<OrderItemEvent> orderItemEvents = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest orderItemRequest : createOrderRequest.getOrderItemRequests()) {
            OrderItem orderItem = orderItemService
                        .createOrderItem(orderItemRequest, savedOrder);
            totalAmount = totalAmount.add(orderItem.getSubTotal());
            OrderItemEvent orderItemEvent = orderMapper.toEvent(orderItem);
            orderItemEvents.add(orderItemEvent);
        }
        savedOrder.setTotalAmount(totalAmount);
        Order updatedOrder = orderRepository.save(savedOrder);
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(updatedOrder.getId())
                .orderNumber(updatedOrder.getOrderNumber())
                .customerId(updatedOrder.getCustomerId())
                .totalAmount(totalAmount)
                .orderItemEvents(orderItemEvents)
                .build();
        outboxEventService.saveEvent(EventType.ORDER_CREATED, 
                                    AggregateType.ORDER,
                                    savedOrder.getId().toString(), 
                                    orderCreatedEvent);  
        return orderMapper.toResponse(updatedOrder);

    }

    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> 
                    new ResourceNotFoundException("order not found"));
    }

    public List<OrderResponse> getAllOrders(Long customerId) {
        log.info("Getting all orders for customer with id {}", customerId);
        return orderRepository.findByCustomerId(customerId)
                    .stream()
                    .map(order -> orderMapper.toResponse(order))
                    .collect(Collectors.toList());

    }

    public void deleteOrder(Long orderId) {
        log.info("deleting order with id {}", orderId);
        orderRepository.deleteById(orderId);
    }
    
    public void updateStatus(Long orderId, OrderStatus orderStatus) {
        Order order = findOrder(orderId);
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getId(), orderStatus);

        List<OrderItem> orderItems = orderItemService.findAllOrderItems(order);
            List<OrderItemEvent> orderItemEvents = orderItems.stream()
                    .map(orderItem -> orderMapper.toEvent(orderItem))
                    .collect(Collectors.toList());

        if (orderStatus == OrderStatus.PENDING_PAYMENT) {
            // initiate payment
            OrderPaymentEvent orderPaymentEvent = OrderPaymentEvent.builder()
                    .orderId(orderId)
                    .orderNumber(order.getOrderNumber())
                    .customerId(order.getCustomerId())
                    .totalAmount(order.getTotalAmount())
                    .build();
            outboxEventService.saveEvent(
                    EventType.ORDER_PAYMENT_INITIATED, 
                    AggregateType.ORDER, 
                    order.getId().toString(), 
                    orderPaymentEvent);
        } else if (orderStatus == OrderStatus.CONFIRMED) {
            // confirm inventory
            OrderConfirmedEvent orderConfirmedEvent = OrderConfirmedEvent.builder()
                    .orderId(orderId)
                    .customerId(order.getCustomerId())
                    .totalAmount(order.getTotalAmount())
                    .orderItemEvents(orderItemEvents)
                    .build();
            outboxEventService.saveEvent(
                    EventType.ORDER_CONFIRMED,
                    AggregateType.ORDER, 
                    order.getId().toString(), 
                    orderConfirmedEvent);
        } else if (orderStatus == OrderStatus.CANCELLED) {
            //cancel inventory
            OrderCancelledEvent orderCancelledEvent = OrderCancelledEvent.builder()
                    .orderId(orderId)
                    .customerId(order.getCustomerId())
                    .orderItemEvents(orderItemEvents)
                    .build();
            outboxEventService.saveEvent(
                    EventType.ORDER_CANCELLED,
                    AggregateType.ORDER, 
                    order.getId().toString(), 
                    orderCancelledEvent);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
