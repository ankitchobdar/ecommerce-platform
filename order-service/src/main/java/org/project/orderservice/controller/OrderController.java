package org.project.orderservice.controller;

import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/process")
    public OrderDTO processOrder(@RequestBody Order order) {
        return orderService.processOrder(order);
    }

    @GetMapping("/confirm")
    public OrderDTO confirmOrder(@RequestParam String orderId) {
        return orderService.confirmOrder(orderId);
    }

    @GetMapping("/cancel")
    public OrderDTO cancelOrder(@RequestParam String orderId) {
        return orderService.cancelOrder(orderId);
    }
}
