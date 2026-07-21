package org.project.inventoryservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.Item;
import org.project.common.Status;
import org.project.common.inventory.InventoryDTO;
import org.project.common.utility.MessageUtility;
import org.project.inventoryservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public InventoryDTO checkInventory(List<Item> items) {
        log.info("Checking inventory {}", items);
        //Check inventory for each items
        items.forEach(item -> {
            Item inventoryItem = inventoryRepository.getItemByItemId(item.getItemId());
            if (inventoryItem == null) {
                item.setActualQuantity(0);
            } else {
                log.info("Checking inventory {}", inventoryItem.getItemId());
                item.setActualQuantity(inventoryItem.getActualQuantity());
            }
        });
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory checked successfully"), items);
    }

    @Transactional
    public InventoryDTO updateInventory(List<Item> items) {
        log.info("Updating inventory for items: {}", items);
        inventoryRepository.saveAll(items);
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory updated successfully"), items);
    }

    @Transactional
    public InventoryDTO addInventory(List<Item> items) {
        log.info("Adding inventory for items: {}", items);
        inventoryRepository.saveAll(items);
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory added successfully"), items);
    }
}
