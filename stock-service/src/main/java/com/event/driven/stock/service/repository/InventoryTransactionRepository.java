package com.event.driven.stock.service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.event.driven.stock.service.entity.InventoryTransaction;
import com.event.driven.stock.service.entity.Product;
import com.event.driven.stock.service.enums.TransactionType;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    List<InventoryTransaction> findByProduct(Product product);

    List<InventoryTransaction> findByTransactionType(TransactionType transactionType);
}
