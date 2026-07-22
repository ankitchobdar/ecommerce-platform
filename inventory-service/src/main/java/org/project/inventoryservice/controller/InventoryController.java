package org.project.inventoryservice.controller;

import org.project.common.Item;
import org.project.common.inventory.InventoryDTO;
import org.project.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
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
}
