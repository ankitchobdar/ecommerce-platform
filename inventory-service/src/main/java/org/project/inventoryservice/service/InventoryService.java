package org.project.inventoryservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.events.InventoryReservedEvent;
import org.project.common.events.InventoryUpdatedEvent;
import org.project.common.inventory.*;
import org.project.common.Status;
import org.project.common.utility.MessageUtility;
import org.project.inventoryservice.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
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

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value("${spring.kafka.template.order-reply-topic}")
    private String orderReplyTopic;

    @Transactional
    public InventoryDTO addInventory(List<Item> items) {
        log.info("Adding inventory for items: {}", items);
        items.forEach(item -> {item.setCreatedAt(LocalDateTime.now());});
        inventoryRepository.saveAll(items);
        return new InventoryDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Inventory added successfully"), items);
    }

    @Transactional
    @KafkaListener(topics = "${spring.kafka.template.inventory-process-topic}", groupId = "inventory-service-group")
    public void reserveInventory(InventoryReservedEvent event) {
        log.info("[KAFKA] Reserving inventory {}", event);
        InventoryRequestDTO inventoryRequestDTO = new InventoryRequestDTO(event.getSagaId(), event.getItems());
        // Convert event to inventory request DTO
        InventoryResponseDTO inventoryResponseDTO = reserveInventory(inventoryRequestDTO);
        kafkaProducerService.sendMessage(orderReplyTopic, "inventory-service", InventoryUpdatedEvent.builder()
                .sagaId(event.getSagaId())
                .orderId(event.getOrderId())
                .items(inventoryRequestDTO.getItems())
                .outOfStockItems(inventoryResponseDTO.getOutOfStockItems())
                .status(inventoryResponseDTO.getStatus())
                .build()
        );
    }

    @Transactional
    public InventoryResponseDTO reserveInventory(InventoryRequestDTO inventoryRequestDTO) {
        log.info("Reserving inventory {}", inventoryRequestDTO);
        List<Item> outOfStockItems = new ArrayList<>();
        String reservationId = UUID.randomUUID().toString();
        // Implementation for reserving inventory
        for (Item item : inventoryRequestDTO.getItems()) {
            Item itemFromDB = inventoryRepository.getItemById(item.getId());
            if (itemFromDB == null) {
                outOfStockItems.add(item);
            } else if (itemFromDB.getQuantity() < item.getQuantity()) {
                outOfStockItems.add(item);
            } else {
                item.setId(itemFromDB.getId());
                item.setReservedQuantity(item.getQuantity());
                item.setQuantity(itemFromDB.getQuantity() - item.getReservedQuantity());
                item.setReservationId(reservationId);
                item.setCreatedAt(itemFromDB.getCreatedAt());
            }
        }
        List<Item> successfulReservations = inventoryRequestDTO.getItems().stream()
            .filter(item -> !outOfStockItems.contains(item))
            .toList();
        inventoryRepository.saveAll(successfulReservations);

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
