package org.project.orderservice.controller;

import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.order.OrderRequestDTO;
import org.project.common.order.OrderResponseDTO;
import org.project.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/process")
    public OrderResponseDTO processOrder(@RequestBody OrderRequestDTO order) {
        return orderService.processOrder(order);
    }

    @GetMapping("/reverse")
    public OrderResponseDTO reverseOrder(@RequestParam Long orderId) {
        return orderService.reverseOrder(orderId);
    }
}
