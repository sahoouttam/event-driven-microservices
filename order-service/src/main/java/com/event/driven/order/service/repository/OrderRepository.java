package com.event.driven.order.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.order.service.entity.Order;

@Repository
public interface OrderRepository  extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerId(Long customerId);
}
