package com.event.driven.stock.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.event.driven.stock.service.entity.Inventory;
import com.event.driven.stock.service.entity.InventoryTransaction;
import com.event.driven.stock.service.enums.TransactionType;
import com.event.driven.stock.service.repository.InventoryTransactionRepository;

@Service
public class InventoryTransactionService {
    
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    public InventoryTransactionService(InventoryTransactionRepository inventoryTransactionRepository) {
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    public InventoryTransaction saveTransaction(Inventory inventory, Integer quantity, TransactionType transactionType) {
        InventoryTransaction inventoryTransaction = InventoryTransaction.builder()
                                .inventory(inventory)
                                .quantity(quantity)
                                .transactionType(transactionType)
                                .build();
        return inventoryTransactionRepository.save(inventoryTransaction);
    }
    
    public InventoryTransaction save(InventoryTransaction inventoryTransaction) {
        return inventoryTransactionRepository.save(inventoryTransaction);
    }
}
