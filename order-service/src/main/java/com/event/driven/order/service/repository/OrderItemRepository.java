package com.event.driven.order.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.order.service.entity.Order;
import com.event.driven.order.service.entity.OrderItem;

@Repository
public interface OrderItemRepository  extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrder(Order order);
}
