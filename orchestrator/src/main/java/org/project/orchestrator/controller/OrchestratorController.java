package org.project.orchestrator.controller;

import org.project.common.inventory.InventoryDTO;
import org.project.common.order.Order;
import org.project.orchestrator.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator")
public class OrchestratorController {

    @Autowired
    private OrchestratorService orchestratorService;

    @PostMapping("/process")
    public InventoryDTO processOrder(@RequestBody Order order) {
        return orchestratorService.processOrder(order);
    }

    @PostMapping("/reverse")
    public String reverseOrder() {
        return orchestratorService.reverseOrder();
    }
}
