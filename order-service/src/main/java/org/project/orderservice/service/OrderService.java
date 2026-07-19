package org.project.orderservice.service;

import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.order.OrderStatus;
import org.project.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderDTO processOrder(Order order) {
        orderRepository.save(order);
        return new OrderDTO(null, order.getOrderId());
    }

    public OrderDTO confirmOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null)
            order.setOrderStatus(OrderStatus.COMPLETED);
        else
            return new OrderDTO(null, -1L);
        return new OrderDTO(null, order.getOrderId());
    }

    public OrderDTO cancelOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null)
            order.setOrderStatus(OrderStatus.CANCELED);
        else
            return new OrderDTO(null, -1L);
        return new OrderDTO(null, order.getOrderId());
    }
}
