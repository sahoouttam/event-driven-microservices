package com.event.driven.stock.service.service;

import java.util.List;

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

    public void saveTransaction(Inventory inventory, Integer quantity, TransactionType transactionType) {
        InventoryTransaction inventoryTransaction = InventoryTransaction.builder()
                                .inventory(inventory)
                                .quantity(quantity)
                                .transactionType(transactionType)
                                .build();
        inventoryTransactionRepository.save(inventoryTransaction);
    }
    
    public InventoryTransaction save(InventoryTransaction inventoryTransaction) {
        return inventoryTransactionRepository.save(inventoryTransaction);
    }

    public List<InventoryTransaction> findAllInventoryTransactions(Inventory inventory) {
        return inventoryTransactionRepository.findByInventory(inventory);
    }
}
