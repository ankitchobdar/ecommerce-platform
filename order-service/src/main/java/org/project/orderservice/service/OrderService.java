package org.project.orderservice.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.events.InventoryReservedEvent;
import org.project.common.events.InventoryUpdatedEvent;
import org.project.common.events.OrderCompletedEvent;
import org.project.common.events.OrderInitiatedEvent;
import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.Status;
import org.project.common.order.OrderRequestDTO;
import org.project.common.order.OrderResponseDTO;
import org.project.common.utility.MessageUtility;
import org.project.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Value("${spring.kafka.template.order-reply-topic}")
    private String orderReplyTopic;

    @Transactional
    @KafkaListener(topics = "${spring.kafka.template.order-process-topic}", groupId = "order-service-group")
    public void processOrder(OrderInitiatedEvent event) {
        log.info("[KAFKA] Initiating order {}", event);
        OrderRequestDTO orderRequestDTO = OrderRequestDTO.builder()
                .sagaId(event.getSagaId())
                .orderId(event.getOrderId())
                .items(event.getItems())
                .total(event.getTotal())
                .build();
        OrderResponseDTO orderResponseDTO = processOrder(orderRequestDTO);
        // Notify orchestrator about order processing result
        kafkaProducerService.sendMessage(orderReplyTopic, "order-service", OrderCompletedEvent.builder()
                .sagaId(event.getSagaId())
                .orderId(event.getOrderId())
                .total(event.getTotal())
                .paymentType(event.getPaymentType())
                .status(orderResponseDTO.getOrderStatus())
                .build());
    }

    public OrderResponseDTO processOrder(OrderRequestDTO orderRequestDTO) {
        log.info("Order service processing order {}", orderRequestDTO);
        Order order = null;
        try {
            order = Order.builder()
                    .orderId(orderRequestDTO.getOrderId())
                    .sagaId(orderRequestDTO.getSagaId())
                    .items(orderRequestDTO.getItems())
                    .total(orderRequestDTO.getTotal())
                    .orderStatus(Status.COMPLETED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            orderRepository.save(order);
            return new OrderResponseDTO(
                    MessageUtility.getBaseMessage(Status.SUCCESS, "Order processed successfully"),
                    null,
                    order.getOrderId(),
                    order.getOrderStatus(),
                    order.getTotal()
            );
        } catch (Exception e) {
            log.error("Error processing order: {}", e.getMessage());
            //Notify orchestrator about failure
            return new OrderResponseDTO(
                    MessageUtility.getBaseMessage(Status.FAILED, "Order not found!"),
                    null,
                    null,
                    Status.FAILED,
                    null
            );
        }
    }


    public OrderResponseDTO reverseOrder(Long orderId) {
        log.info("Order service reversing order {}", orderId);
        Order order =  null;
        try {
            order = orderRepository.findByOrderId(orderId);
            if (order != null) {
                order.setOrderStatus(Status.CANCELED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                return new OrderResponseDTO(
                        MessageUtility.getBaseMessage(Status.SUCCESS, "Order reversed successfully"),
                        order.getSagaId(),
                        order.getOrderId(),
                        order.getOrderStatus(),
                        order.getTotal()
                );
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
                MessageUtility.getBaseMessage(Status.FAILED, "Order not found!"),
                null,
                null,
                Status.FAILED,
                null
        );
    }
}
