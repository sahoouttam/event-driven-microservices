package com.event.driven.fulfillment.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.fulfillment.service.entity.Return;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    
}
