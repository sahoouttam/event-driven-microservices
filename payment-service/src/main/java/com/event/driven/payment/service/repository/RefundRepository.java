package com.event.driven.payment.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.payment.service.entity.Payment;
import com.event.driven.payment.service.entity.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    List<Refund> findByPayment(Payment payment);
}
