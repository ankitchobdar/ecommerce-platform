package org.project.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.Status;
import org.project.common.utility.MessageUtility;
import org.project.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public OrderDTO processOrder(Order order) {
        log.info("Order service processing order {}", order);
        orderRepository.save(order);
        return new OrderDTO(MessageUtility.getBaseMessage(Status.SUCCESS, "Order added successfully"),
                order.getOrderId());
    }

    @Transactional
    public OrderDTO confirmOrder(String orderId) {
        log.info("Order service confirming order {}", orderId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null)
            order.setOrderStatus(Status.COMPLETED);
        else
            return new OrderDTO(MessageUtility.getBaseMessage(Status.FAILED, "Order not found"), -1L);
        return new OrderDTO(MessageUtility.getBaseMessage(Status.SUCCESS, "Order confirmed successfully"),
                order.getOrderId());
    }

    @Transactional
    public OrderDTO cancelOrder(String orderId) {
        log.info("Order service canceling order {}", orderId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null)
            order.setOrderStatus(Status.CANCELED);
        else
            return new OrderDTO(MessageUtility.getBaseMessage(Status.FAILED, "Order not found"), -1L);
        return new OrderDTO(MessageUtility.getBaseMessage(Status.SUCCESS, "Order canceled successfully"),
                order.getOrderId());
    }
}
