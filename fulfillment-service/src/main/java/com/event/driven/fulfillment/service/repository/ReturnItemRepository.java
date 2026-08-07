package com.event.driven.fulfillment.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.fulfillment.service.entity.Return;
import com.event.driven.fulfillment.service.entity.ReturnItem;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    
    List<ReturnItem> findByReturnEntity(Return returnEntity);
}
