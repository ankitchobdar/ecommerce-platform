package org.project.inventoryservice.service;

import jakarta.transaction.Transactional;
import org.project.common.Item;
import org.project.common.Status;
import org.project.common.inventory.InventoryDTO;
import org.project.common.utility.MessageUtility;
import org.project.inventoryservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public InventoryDTO checkInventory(List<Item> items) {
        //Check inventory for each items
        items.forEach(item -> {
            Item inventoryItem = inventoryRepository.getItemByItemId(item.getItemId());
            if (inventoryItem == null) {
                item.setActualQuantity(0);
            } else {
                item.setActualQuantity(inventoryItem.getActualQuantity());
            }
        });
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.COMPLETED, "Inventory checked successfully"), items);
    }

    @Transactional
    public InventoryDTO updateInventory(List<Item> items) {
        inventoryRepository.saveAll(items);
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.COMPLETED, "Inventory updated successfully"), items);
    }
}
