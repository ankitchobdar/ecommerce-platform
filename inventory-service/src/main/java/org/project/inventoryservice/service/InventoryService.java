package org.project.inventoryservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.inventory.InventoryRequestDTO;
import org.project.common.inventory.InventoryResponseDTO;
import org.project.common.inventory.Item;
import org.project.common.Status;
import org.project.common.inventory.InventoryDTO;
import org.project.common.utility.MessageUtility;
import org.project.inventoryservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                item.setActualQuantity(inventoryItem.getQuantity());
            }
        });
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory checked successfully"), items);
    }

    @Transactional
    public InventoryDTO updateInventory(List<Item> items) {
        List<Item> originalItems = items.stream().map(Item::clone).toList();
        log.info("Updating inventory for items: {}", items);
        items.forEach(item -> item.setQuantity(item.getActualQuantity() - item.getQuantity()));
        inventoryRepository.saveAll(items);
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory updated successfully"), originalItems);
    }

    @Transactional
    public InventoryDTO addInventory(List<Item> items) {
        log.info("Adding inventory for items: {}", items);
        inventoryRepository.saveAll(items);
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory added successfully"), items);
    }

    //NEW APPROACH
    @Transactional
    public InventoryResponseDTO reserveInventory(InventoryRequestDTO inventoryRequestDTO) {
        log.info("Reserving inventory {}", inventoryRequestDTO);
        List<Item> outOfStockItems = new ArrayList<>();
        String reservationId = UUID.randomUUID().toString();
        // Implementation for reserving inventory
        for (Item item : inventoryRequestDTO.getItems()) {
            Item itemFromDB = inventoryRepository.getItemByItemId(item.getItemId());
            if (itemFromDB != null) {
                if(itemFromDB.getQuantity() < item.getQuantity()) {
                    outOfStockItems.add(item);
                } else {
                    item.setItemId(itemFromDB.getItemId());
                    item.setReservedQuantity(item.getQuantity());
                    item.setQuantity(itemFromDB.getQuantity() - item.getReservedQuantity());
                    item.setReservationId(reservationId);
                    item.setUpdatedAt(LocalDateTime.now());
                }
            }
        }
        inventoryRepository.saveAll(inventoryRequestDTO.getItems());

        inventoryRequestDTO.getItems().forEach(item -> {
            item.setQuantity(item.getReservedQuantity());
        });

        if(outOfStockItems.isEmpty()) {
            return new InventoryResponseDTO(
                    MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory reserved successfully"),
                    inventoryRequestDTO.getItems(),
                    outOfStockItems,
                    Status.SUCCESS);
        } else {
            return new InventoryResponseDTO(
                    MessageUtility.getBaseMessage(Status.FAILED, "Some items are out of stock"),
                    inventoryRequestDTO.getItems(),
                    outOfStockItems,
                    Status.FAILED);
        }
    }

    @Transactional
    public InventoryResponseDTO updateInventory(String reservationId) {
        log.info("Updating inventory for reservation {}", reservationId);
        // Implementation for updating inventory
        List<Item> itemsToUpdate = inventoryRepository.getItemByReservationId(reservationId);
        if (itemsToUpdate != null && !itemsToUpdate.isEmpty()) {
            itemsToUpdate.forEach(item -> {
                item.setQuantity(item.getQuantity() - item.getReservedQuantity());
                item.setReservedQuantity(0);
                item.setReservationId(null);
                item.setUpdatedAt(LocalDateTime.now());
            });
            inventoryRepository.saveAll(itemsToUpdate);
            return new InventoryResponseDTO(
                    MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory updated successfully"),
                    itemsToUpdate,
                    new ArrayList<>(),
                    Status.SUCCESS);
        }
        return new InventoryResponseDTO(
                MessageUtility.getBaseMessage(Status.FAILED, "No items found for update"),
                new ArrayList<>(),
                new ArrayList<>(),
                Status.FAILED
        );
    }

    @Transactional
    public InventoryResponseDTO releaseInventory(String reservationId) {
        log.info("Releasing inventory for reservation {}", reservationId);
        // Implementation for releasing inventory
        List<Item> itemsToRelease = inventoryRepository.getItemByReservationId(reservationId);
        if (itemsToRelease != null && !itemsToRelease.isEmpty()) {
            itemsToRelease.forEach(item -> {
                item.setQuantity(item.getReservedQuantity() + item.getQuantity());
                item.setReservationId(null);
                item.setReservedQuantity(0);
                item.setUpdatedAt(LocalDateTime.now());
            });
            inventoryRepository.saveAll(itemsToRelease);
            return new InventoryResponseDTO(
                    MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory released successfully"),
                    itemsToRelease,
                    new ArrayList<>(),
                    Status.SUCCESS);
        }
        return new InventoryResponseDTO(
                MessageUtility.getBaseMessage(Status.FAILED, "No items found for release"),
                new ArrayList<>(),
                new ArrayList<>(),
                Status.FAILED
        );
    }
}
