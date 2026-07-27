package org.project.inventoryservice.controller;

import org.project.common.inventory.InventoryRequestDTO;
import org.project.common.inventory.InventoryResponseDTO;
import org.project.common.inventory.Item;
import org.project.common.inventory.InventoryDTO;
import org.project.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/checkInventory")
    public InventoryDTO checkInventory(@RequestBody List<Item> items) {
        return inventoryService.checkInventory(items);
    }

    @PostMapping("/updateInventory")
    public InventoryDTO updateInventory(@RequestBody List<Item> items) {
        return inventoryService.updateInventory(items);
    }

    @PostMapping("/addInventory")
    public InventoryDTO addInventory(@RequestBody List<Item> items) {
        return inventoryService.addInventory(items);
    }

    //NEW APPROACH
    @PostMapping("/reservation")
    public InventoryResponseDTO reserveInventory(@RequestBody InventoryRequestDTO inventoryRequestDTO) {
        return inventoryService.reserveInventory(inventoryRequestDTO);
    }

    @GetMapping("/update")
    public InventoryResponseDTO updateInventory(@RequestParam String reservationId) {
        return inventoryService.updateInventory(reservationId);
    }

    @GetMapping("/release")
    public InventoryResponseDTO releaseInventory(@RequestParam String reservationId) {
        return inventoryService.releaseInventory(reservationId);
    }
}
