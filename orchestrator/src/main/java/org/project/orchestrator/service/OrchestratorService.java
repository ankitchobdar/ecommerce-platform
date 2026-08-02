package org.project.orchestrator.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.Status;
import org.project.common.events.*;
import org.project.common.inventory.InventoryResponseDTO;
import org.project.common.saga.SagaEvent;
import org.project.common.orchestrator.OrchestratorRequestDTO;
import org.project.common.orchestrator.OrchestratorResponseDTO;
import org.project.common.saga.Saga;
import org.project.common.saga.SagaStatus;
import org.project.orchestrator.exception.ServiceException;
import org.project.orchestrator.repository.OrchestratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.project.common.utility.MessageUtility;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OrchestratorService {

    @Value("${spring.kafka.template.order-process-topic}")
    private String orderEvents;

    @Value("${spring.kafka.template.payment-process-topic}")
    private String paymentEvents;

    @Value("${spring.kafka.template.inventory-process-topic}")
    private String inventoryEvents;

    @Value("${spring.kafka.template.compensation-process-topic}")
    private String compensationEvents;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private OrchestratorRepository orchestratorRepository;

    private RestClient restClient;

    // Map to hold pending saga results for request-reply pattern
    private final Map<Long, CompletableFuture<OrchestratorResponseDTO>> pendingSagas = new ConcurrentHashMap<>();

    OrchestratorService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

//    @Transactional
//    public ProcessOrderDTO process(Order order) {
//        boolean outOfStock = false;
//        ProcessOrderDTO processOrderDTO = ProcessOrderDTO.builder().build();
//        try {
//            //checkInventory
//            InventoryDTO inventoryDTO = restClient.post().uri("http://localhost:8083/inventory/checkInventory")
//                    .body(order.getItems())
//                    .retrieve()
//                    .body(InventoryDTO.class);
//
//            Item item = inventoryDTO.items().stream()
//                    .filter(i -> i.getActualQuantity() - i.getQuantity() < 0)
//                    .findFirst()
//                    .orElse(null);
//
//            if(item != null)
//                log.info("Item with insufficient quantity: {}", item);
//
//            if (item == null) {
//                //processOrder
//                OrderDTO orderDTO = restClient.post().uri("http://localhost:8081/order/process")
//                        .body(order)
//                        .retrieve()
//                        .body(OrderDTO.class);
//                //confirmOrder
//                OrderDTO confirmedOrder = restClient.get()
//                        .uri("http://localhost:8081/order/confirm?orderId=" + orderDTO.orderId())
//                        .retrieve()
//                        .body(OrderDTO.class);
//                //processPayment
//                PaymentDTO paymentDTO = restClient.post().uri("http://localhost:8082/payment/process")
//                        .body(Payment.builder()
//                                .orderId(confirmedOrder.orderId())
//                                .paymentType(order.getPaymentType())
//                                .paymentStatus(Status.PENDING)
//                                .build())
//                        .retrieve()
//                        .body(PaymentDTO.class);
//                //confirmPayment
//                PaymentDTO confirmedPayment = restClient.get()
//                        .uri("http://localhost:8082/payment/confirm?paymentId=" + paymentDTO.paymentId())
//                        .retrieve()
//                        .body(PaymentDTO.class);
//                //updateInventory
//
//                InventoryDTO updatedInventory = restClient.post()
//                        .uri("http://localhost:8083/inventory/updateInventory")
//                        .body(inventoryDTO.items())
//                        .retrieve()
//                        .body(InventoryDTO.class);
//
//                processOrderDTO.setBaseMessage(MessageUtility.getBaseMessage(Status.SUCCESS, "Order processed successfully"));
//                processOrderDTO.setItems(updatedInventory.items());
//                processOrderDTO.setTotalPrice(updatedInventory.items().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
//                processOrderDTO.setPaymentStatus(confirmedPayment.paymentStatus());
//            } else
//                outOfStock = true;
//        } catch (Exception e) {
//            log.error("Error processing order {}", order, e);
//            //reverseOrder(orderId);
//            throw new ServiceException("Error processing order", "500", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        if (outOfStock) {
//            throw new InventoryExhaustedException("Item with insufficient quantity", "500", HttpStatus.OK);
//        }
//        return processOrderDTO;
//    }
//
//    @Transactional
//    public ProcessOrderDTO processOrder(Order order) {
//        ProcessOrderDTO processOrderDTO = ProcessOrderDTO.builder().build();
//        try {
//            //Check inventory
//            InventoryDTO inventoryDTO = restClient.post().uri("http://localhost:8083/inventory/checkInventory")
//                    .body(order.getItems())
//                    .retrieve()
//                    .body(InventoryDTO.class);
//
//            assert inventoryDTO != null;
//            List<Item> outOfStockItems = inventoryDTO.items().stream()
//                    .filter(i -> i.getActualQuantity() - i.getQuantity() < 0)
//                    .toList();
//            if (!outOfStockItems.isEmpty()) {
//                return ProcessOrderDTO.builder()
//                        .baseMessage(MessageUtility.getBaseMessage(Status.FAILED, "Item with insufficient quantity"))
//                        .items(outOfStockItems)
//                    .build();
//            }
//            //Confirm order
//            //Process payment
//            //Update inventory
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return processOrderDTO;
//    }
//
//    public ProcessOrderDTO sendPayment(Order order) {
//        kafkaProducerService.sendMessage("my-topic", order);
//        return ProcessOrderDTO.builder()
//                .baseMessage(MessageUtility.getBaseMessage(Status.SUCCESS, "Payment sent successfully"))
//                .build();
//    }

    //NEW APPROACH
    @Transactional
    public OrchestratorResponseDTO processOrder(OrchestratorRequestDTO requestDTO) {
        OrchestratorResponseDTO orchestratorResponseDTO = new OrchestratorResponseDTO();
        try {
            log.info("[KAFKA] Processing order {}", requestDTO);
            String orderId = UUID.randomUUID().toString();
            Saga saga = Saga.builder()
                    .status(SagaStatus.SAGA_INITIATED)
                    .orderId(orderId)
                    .build();
            orchestratorRepository.save(saga);

            Long sagaId = saga.getSagaId();

            // prepare future for request-reply
            CompletableFuture<OrchestratorResponseDTO> future = new CompletableFuture<>();
            pendingSagas.put(sagaId, future);

            //Reserve Inventory
            InventoryReservedEvent event = new InventoryReservedEvent(
                    sagaId,
                    orderId,
                    LocalDateTime.now(),
                    null,
                    requestDTO.getItems());
            kafkaProducerService.sendMessage(inventoryEvents, "inventory-service", event);

            saga.setStatus(SagaStatus.INVENTORY_RESERVATION_INITIATED);
            orchestratorRepository.save(saga);

            // Wait for saga to complete (request-reply) with timeout
            try {
                OrchestratorResponseDTO result = future.get(30, java.util.concurrent.TimeUnit.SECONDS);
                return result;
            } catch (java.util.concurrent.TimeoutException te) {
                // remove pending future
                pendingSagas.remove(sagaId);

                // mark saga as failed and persist
                Saga timeoutSaga = orchestratorRepository.findSagaBySagaId(sagaId)
                        .orElse(null);
                if (timeoutSaga != null) {
                    timeoutSaga.setStatus(SagaStatus.SAGA_FAILED);
                    orchestratorRepository.save(timeoutSaga);

                    // publish compensation event so other services can rollback
                    OrderCancelledEvent cancelEvent = new OrderCancelledEvent();
                    cancelEvent.sagaId = sagaId;
                    cancelEvent.orderId = timeoutSaga.getOrderId();
                    cancelEvent.createdAt = LocalDateTime.now();
                    kafkaProducerService.sendMessage(compensationEvents, "orchestrator-service", cancelEvent);
                }

                orchestratorResponseDTO.setSagaId(sagaId);
                orchestratorResponseDTO.setBaseMessage(MessageUtility.getBaseMessage(Status.FAILED, "Saga timed out; compensation initiated"));
                return orchestratorResponseDTO;
            } catch (InterruptedException | java.util.concurrent.ExecutionException ex) {
                pendingSagas.remove(sagaId);
                log.error("Error waiting for saga result", ex);
                throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            log.error("Error processing order", e);
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Kafka listener to receive saga status updates from the saga-topic
     */
    @KafkaListener(topics = "${spring.kafka.template.order-reply-topic}", groupId = "orchestrator-group")
    public void handleSagaEvent(SagaEvent sagaEvent) {
        log.info("Received saga event: {}", sagaEvent);

        switch (sagaEvent.getClass().getSimpleName()) {
        case "InventoryUpdatedEvent":
            // Handle inventory updated event
            handleInventoryUpdate((InventoryUpdatedEvent) sagaEvent);
            break;
        case "OrderCompletedEvent":
            // Handle order completed event
            handleOrderCompletion((OrderCompletedEvent) sagaEvent);
            break;
        case "PaymentCompletedEvent":
            // Handle payment completed event
            handlePaymentCompletion((PaymentCompletedEvent) sagaEvent);
            break;
        // Add more cases as needed
        }

    }

    private void handleInventoryUpdate(InventoryUpdatedEvent sagaEvent) {
        Saga saga = orchestratorRepository.findSagaBySagaId(sagaEvent.getSagaId())
                .orElseThrow(() -> new RuntimeException("Saga not found for sagaId: " + sagaEvent.getSagaId()));
        saga.setStatus(SagaStatus.INVENTORY_RESERVATION_COMPLETED);
        orchestratorRepository.save(saga);

        if(sagaEvent.getStatus().equals(Status.SUCCESS)) {
            // Proceed to next step in the saga, e.g., process order
            OrderInitiatedEvent orderInitiatedEvent = OrderInitiatedEvent.builder()
                    .sagaId(sagaEvent.getSagaId())
                    .orderId(sagaEvent.getOrderId())
                    .items(sagaEvent.getItems())
                    .total(sagaEvent.getTotal())
                    //.paymentType(sagaEvent.getPaymentType())
                    .build();

            kafkaProducerService.sendMessage(orderEvents, "orchestrator-service", orderInitiatedEvent);
            saga.setStatus(SagaStatus.ORDER_INITIATED);
            orchestratorRepository.save(saga);
        } else {
            // Initiated Rollback of Inventory and complete pending future with failure
            CompletableFuture<OrchestratorResponseDTO> pending = pendingSagas.remove(sagaEvent.getSagaId());
            if (pending != null) {
                OrchestratorResponseDTO resp = new OrchestratorResponseDTO();
                resp.setSagaId(sagaEvent.getSagaId());
                resp.setBaseMessage(MessageUtility.getBaseMessage(Status.FAILED, "Inventory update failed"));
                pending.complete(resp);
            }

            // trigger compensation/rollback across services
            initiateCompensation(sagaEvent.getSagaId(), saga.getOrderId(), "Inventory update failed");

            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Inventory update failed");
        }
    }

    private void handleOrderCompletion(OrderCompletedEvent sagaEvent) {
        Saga saga = orchestratorRepository.findSagaBySagaId(sagaEvent.getSagaId())
                .orElseThrow(() -> new RuntimeException("Saga not found for sagaId: " + sagaEvent.getSagaId()));
        saga.setStatus(SagaStatus.INVENTORY_RESERVATION_COMPLETED);
        orchestratorRepository.save(saga);

        if(sagaEvent.getStatus().equals(Status.SUCCESS)) {
            PaymentInitiatedEvent paymentInitiatedEvent = PaymentInitiatedEvent.builder()
                    .sagaId(sagaEvent.getSagaId())
                    .orderId(sagaEvent.getOrderId())
                    .total(sagaEvent.getTotal())
                    .paymentType(sagaEvent.getPaymentType())
                    .build();

            kafkaProducerService.sendMessage(orderEvents, "orchestrator-service", paymentInitiatedEvent);
            saga.setStatus(SagaStatus.PAYMENT_INITIATED);
            orchestratorRepository.save(saga);
        } else {
            // Initiated Rollback of Order and Inventory and complete pending future with failure
            CompletableFuture<OrchestratorResponseDTO> pending = pendingSagas.remove(sagaEvent.getSagaId());
            if (pending != null) {
                OrchestratorResponseDTO resp = new OrchestratorResponseDTO();
                resp.setSagaId(sagaEvent.getSagaId());
                resp.setBaseMessage(MessageUtility.getBaseMessage(Status.FAILED, "Order completion failed"));
                pending.complete(resp);
            }

            // trigger compensation/rollback across services
            initiateCompensation(sagaEvent.getSagaId(), saga.getOrderId(), "Order completion failed");

            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Order completion failed");
        }
    }

    private void handlePaymentCompletion(PaymentCompletedEvent sagaEvent) {
        Saga saga = orchestratorRepository.findSagaBySagaId(sagaEvent.getSagaId())
                .orElseThrow(() -> new RuntimeException("Saga not found for sagaId: " + sagaEvent.getSagaId()));
        saga.setStatus(SagaStatus.ORDER_COMPLETED);
        orchestratorRepository.save(saga);

        if(sagaEvent.getStatus().equals(Status.SUCCESS)) {
            log.info("Saga completed successfully for sagaId: {}", sagaEvent.getSagaId());
            CompletableFuture<OrchestratorResponseDTO> pending = pendingSagas.remove(sagaEvent.getSagaId());
            if (pending != null) {
                OrchestratorResponseDTO resp = new OrchestratorResponseDTO();
                resp.setSagaId(sagaEvent.getSagaId());
                resp.setBaseMessage(MessageUtility.getBaseMessage(Status.SUCCESS, "Saga completed successfully"));
                pending.complete(resp);
            }
        } else {
            // Initiated Rollback of Payment, Order and Inventory and complete pending future with failure
            CompletableFuture<OrchestratorResponseDTO> pending = pendingSagas.remove(sagaEvent.getSagaId());
            if (pending != null) {
                OrchestratorResponseDTO resp = new OrchestratorResponseDTO();
                resp.setSagaId(sagaEvent.getSagaId());
                resp.setBaseMessage(MessageUtility.getBaseMessage(Status.FAILED, "Payment completion failed"));
                pending.complete(resp);
            }

            // trigger compensation/rollback across services
            initiateCompensation(sagaEvent.getSagaId(), saga.getOrderId(), "Payment completion failed");

            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Payment completion failed");
        }
    }

    @Transactional
    @KafkaListener(topics = "${spring.kafka.template.compensation-process-topic}", groupId = "orchestrator-group")
    public OrchestratorResponseDTO compensationOrder(SagaEvent event) {
        log.info("Received event {}", event);
        OrchestratorResponseDTO orchestratorResponseDTO = new OrchestratorResponseDTO();

        Saga saga = orchestratorRepository.findSagaBySagaId(event.getSagaId())
                .orElseThrow(() -> new RuntimeException("Saga not found for sagaId: " + event.getSagaId()));
        try {
            //Release Inventory
            //Reverse Order
            //Reverse Payment
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return orchestratorResponseDTO;
    }

    public OrchestratorResponseDTO getSagaStatus(Long sagaId) {
        OrchestratorResponseDTO orchestratorResponseDTO = new OrchestratorResponseDTO();
        Saga saga = orchestratorRepository.findSagaBySagaId(sagaId)
                .orElse(null);
        if (saga == null) {
            orchestratorResponseDTO.setBaseMessage(MessageUtility.getBaseMessage(Status.FAILED, "Saga not found"));
            return orchestratorResponseDTO;
        }
        orchestratorResponseDTO.setSagaId(saga.getSagaId());
        orchestratorResponseDTO.setBaseMessage(MessageUtility.getBaseMessage(Status.SUCCESS, "Saga status: " + saga.getStatus()));
        return orchestratorResponseDTO;
    }

    /**
     * Persist failure and publish compensation events for rollback across services
     */
    private void initiateCompensation(Long sagaId, String orderId, String reason) {
        // mark saga failed
        Saga s = orchestratorRepository.findSagaBySagaId(sagaId).orElse(null);
        if (s != null) {
            s.setStatus(SagaStatus.SAGA_FAILED);
            orchestratorRepository.save(s);
        }

        // publish compensation events
        OrderCancelledEvent orderCancel = new OrderCancelledEvent();
        orderCancel.setSagaId(sagaId);
        orderCancel.setOrderId(orderId);
        orderCancel.setCreatedAt(LocalDateTime.now());
        kafkaProducerService.sendMessage(compensationEvents, "orchestrator-service", orderCancel);

        PaymentCancelledEvent paymentCancel = new PaymentCancelledEvent();
        paymentCancel.setSagaId(sagaId);
        paymentCancel.setOrderId(orderId);
        paymentCancel.setCreatedAt(LocalDateTime.now());
        kafkaProducerService.sendMessage(compensationEvents, "orchestrator-service", paymentCancel);

        InventoryFailedEvent inventoryFail = new InventoryFailedEvent();
        inventoryFail.setSagaId(sagaId);
        inventoryFail.setOrderId(orderId);
        inventoryFail.setCreatedAt(LocalDateTime.now());
        inventoryFail.setInventoryStatus(Status.FAILED);
        kafkaProducerService.sendMessage(compensationEvents, "orchestrator-service", inventoryFail);

        log.info("Compensation initiated for sagaId:{} reason:{}", sagaId, reason);
    }

    public OrchestratorResponseDTO reverseOrder(String orderId) {
        // Implementation for reversing an order
        return new OrchestratorResponseDTO();
    }
}
