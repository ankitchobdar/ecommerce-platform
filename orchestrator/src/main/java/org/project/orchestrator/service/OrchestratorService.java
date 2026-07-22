package org.project.orchestrator.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.Item;
import org.project.common.Status;
import org.project.common.inventory.InventoryDTO;
import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.orchestrator.exception.InventoryExhaustedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class OrchestratorService {

    private RestClient restClient;

    OrchestratorService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Transactional
    public InventoryDTO processOrder(Order order) {
        InventoryDTO updatedInventory = null;
        try {
            //checkInventory
            log.info("Processing order {}", order);
            InventoryDTO inventoryDTO = restClient.post().uri("http://localhost:8083/inventory/checkInventory")
                    .body(order.getItems())
                    .retrieve()
                    .body(InventoryDTO.class);

            Item item = inventoryDTO.items().stream()
                    .filter(i -> i.getActualQuantity() - i.getQuantity() < 0)
                    .findFirst()
                    .orElse(null);

            if(item != null)
                log.info("Item with insufficient quantity: {}", item);

            if (item == null) {
                //processOrder
                OrderDTO orderDTO = restClient.post().uri("http://localhost:8081/order/process")
                        .body(order)
                        .retrieve()
                        .body(OrderDTO.class);
                //confirmOrder
                OrderDTO confirmedOrder = restClient.get()
                        .uri("http://localhost:8081/order/confirm?orderId=" + orderDTO.orderId())
                        .retrieve()
                        .body(OrderDTO.class);
                //processPayment
                PaymentDTO paymentDTO = restClient.post().uri("http://localhost:8082/payment/process")
                        .body(Payment.builder()
                                .orderId(confirmedOrder.orderId())
                                .paymentType(order.getPaymentType())
                                .paymentStatus(Status.PENDING)
                                .build())
                        .retrieve()
                        .body(PaymentDTO.class);
                //confirmPayment
                PaymentDTO confirmedPayment = restClient.get()
                        .uri("http://localhost:8082/payment/confirm?paymentId=" + paymentDTO.paymentId())
                        .retrieve()
                        .body(PaymentDTO.class);
                //updateInventory

                updatedInventory = restClient.post()
                        .uri("http://localhost:8083/inventory/updateInventory")
                        .body(inventoryDTO.items())
                        .retrieve()
                        .body(InventoryDTO.class);

            } else
                throw new InventoryExhaustedException("Item went out of stock", "ITEM_OUT_OF_STOCK", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error processing order {}", order, e);
            reverseOrder();
        }
        return updatedInventory;
    }

    @Transactional
    public String reverseOrder() {
        try {
            //reverseInventory
            //reversePayment
            //reverseOrder
        } catch (Exception e) {
            //log error
        }
        return null;
    }
}
