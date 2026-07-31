package org.project.orchestrator.controller;

import org.project.common.orchestrator.OrchestratorRequestDTO;
import org.project.common.orchestrator.OrchestratorResponseDTO;
import org.project.common.orchestrator.ProcessOrderDTO;
import org.project.common.order.Order;
import org.project.orchestrator.service.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orchestrator")
public class OrchestratorController {

    @Autowired
    private OrchestratorService orchestratorService;

//    @PostMapping("/process")
//    public ProcessOrderDTO processOrder(@RequestBody Order order) {
//        return orchestratorService.processOrder(order);
//    }

    @PostMapping("/processOrder")
    public OrchestratorResponseDTO processOrder(@RequestBody OrchestratorRequestDTO requestDTO) {
        return orchestratorService.processOrder(requestDTO);
    }

    @GetMapping("/reverseOrder")
    public OrchestratorResponseDTO reverseOrder(@RequestParam String orderId) {
        return orchestratorService.reverseOrder(orderId);
    }

}