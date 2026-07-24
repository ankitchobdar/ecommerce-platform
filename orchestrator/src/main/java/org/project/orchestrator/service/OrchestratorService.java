package org.project.orchestrator.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.Item;
import org.project.common.Status;
import org.project.common.inventory.InventoryDTO;
import org.project.common.orchestrator.ProcessOrderDTO;
import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.common.utility.MessageUtility;
import org.project.orchestrator.exception.InventoryExhaustedException;
import org.project.orchestrator.exception.ServiceException;
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
    public ProcessOrderDTO processOrder(Order order) {
        boolean outOfStock = false;
        ProcessOrderDTO processOrderDTO = ProcessOrderDTO.builder().build();
        try {
            //checkInventory
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

                InventoryDTO updatedInventory = restClient.post()
                        .uri("http://localhost:8083/inventory/updateInventory")
                        .body(inventoryDTO.items())
                        .retrieve()
                        .body(InventoryDTO.class);

                processOrderDTO.setBaseMessage(MessageUtility.getBaseMessage(Status.SUCCESS, "Order processed successfully"));
                processOrderDTO.setItems(updatedInventory.items());
                processOrderDTO.setTotalPrice(updatedInventory.items().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
                processOrderDTO.setPaymentStatus(confirmedPayment.paymentStatus());
            } else
                outOfStock = true;
        } catch (Exception e) {
            log.error("Error processing order {}", order, e);
            reverseOrder();
            throw new ServiceException("Error processing order", "500", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (outOfStock) {
            throw new InventoryExhaustedException("Item with insufficient quantity", "500", HttpStatus.OK);
        }
        return processOrderDTO;
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
