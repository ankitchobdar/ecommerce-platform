package org.project.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.Status;
import org.project.common.order.OrderRequestDTO;
import org.project.common.order.OrderResponseDTO;
import org.project.common.utility.MessageUtility;
import org.project.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderResponseDTO processOrder(OrderRequestDTO orderRequestDTO) {
        log.info("Order service processing order {}", orderRequestDTO);
        Order order = null;
        try {
            order = Order.builder()
                    .sagaId(orderRequestDTO.getSagaId())
                    .items(orderRequestDTO.getItems())
                    .total(orderRequestDTO.getTotal())
                    .orderStatus(Status.COMPLETED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage());
            //Notify orchestrator about failure
            //PENDING
            return new OrderResponseDTO(
                    MessageUtility.getBaseMessage(Status.FAILED, "Error processing order: " + e.getMessage()),
                    null,
                    null,
                    null,
                    null
            );
        }

        return new OrderResponseDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Order processed successfully"),
                order.getSagaId(),
                order.getOrderId(),
                order.getOrderStatus(),
                order.getTotal()
        );
    }


    public OrderResponseDTO reverseOrder(Long orderId) {
        log.info("Order service reverseing order {}", orderId);
        Order order =  null;
        try {
            order = orderRepository.findByOrderId(orderId);
            if (order != null) {
                order.setOrderStatus(Status.CANCELED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }
        } catch (Exception e) {
            log.error("Error reversing order: {}", e.getMessage());
            if(order != null)
                return new OrderResponseDTO(
                        MessageUtility.getBaseMessage(Status.FAILED, "Error reversing order: " + e.getMessage()),
                        order.getSagaId(),
                        order.getOrderId(),
                        Status.FAILED,
                        order.getTotal()
                );
            return new OrderResponseDTO(
                    MessageUtility.getBaseMessage(Status.FAILED, "Error reversing order: " + e.getMessage()),
                    null,
                    null,
                    Status.FAILED,
                    null
            );
        }
        return new OrderResponseDTO(
                MessageUtility.getBaseMessage(Status.SUCCESS, "Order reversed successfully"),
                order.getSagaId(),
                order.getOrderId(),
                order.getOrderStatus(),
                order.getTotal()
        );
    }
}
