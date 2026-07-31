package org.project.orchestrator.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.project.common.inventory.InventoryRequestDTO;
import org.project.common.inventory.Item;
import org.project.common.Status;
import org.project.common.inventory.InventoryDTO;
import org.project.common.orchestrator.OrchestratorRequestDTO;
import org.project.common.orchestrator.OrchestratorResponseDTO;
import org.project.common.orchestrator.ProcessOrderDTO;
import org.project.common.order.Order;
import org.project.common.order.OrderDTO;
import org.project.common.order.OrderRequestDTO;
import org.project.common.payment.Payment;
import org.project.common.payment.PaymentDTO;
import org.project.common.saga.Saga;
import org.project.common.saga.SagaStatus;
import org.project.common.utility.MessageUtility;
import org.project.orchestrator.exception.InventoryExhaustedException;
import org.project.orchestrator.exception.ServiceException;
import org.project.orchestrator.repository.OrchestratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Value("${spring.kafka.template.saga-topic}")
    private String sagaEvents;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private OrchestratorRepository orchestratorRepository;

    private RestClient restClient;

    // Map to store latches for each saga, keyed by sagaId
    private final Map<Long, CountDownLatch> sagaLatches = new ConcurrentHashMap<>();

    // Map to store saga responses, keyed by sagaId
    private final Map<Long, Saga> sagaResponses = new ConcurrentHashMap<>();

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
            Saga saga = Saga.builder()
                    .createdAt(java.time.LocalDateTime.now())
                    .status(SagaStatus.ORDER_INITIATED)
                    .build();
            orchestratorRepository.save(saga);

            Long sagaId = saga.getSagaId();

            // Create a latch to wait for saga completion (number of expected events)
            CountDownLatch sagaLatch = new CountDownLatch(1);
            sagaLatches.put(sagaId, sagaLatch);

            try {
                //Reserve Inventory
                InventoryRequestDTO inventoryRequestDTO = new InventoryRequestDTO(
                        sagaId,
                        requestDTO.getItems()
                );
                kafkaProducerService.sendMessage(inventoryEvents, "inventory-service", inventoryRequestDTO);

                //Process Order
                OrderRequestDTO orderRequestDTO = new OrderRequestDTO(
                        sagaId,
                        requestDTO.getItems(),
                        requestDTO.getTotalAmount());
                kafkaProducerService.sendMessage(orderEvents, "order-service", orderRequestDTO);

                //Process Payment
                // (Add payment logic here)

                //Wait for saga events to complete (with timeout)
                boolean completed = sagaLatch.await(30, TimeUnit.SECONDS);

                if (!completed) {
                    log.warn("Saga processing timed out for sagaId: {}", sagaId);
                    throw new ServiceException("Saga processing timeout", "504", HttpStatus.GATEWAY_TIMEOUT);
                }

                // Retrieve the saga response
                Saga sagaResponse = sagaResponses.get(sagaId);
                log.info("Saga completed with status: {}", sagaResponse.getStatus());

                // You can now use sagaResponse to build the orchestratorResponseDTO
                orchestratorResponseDTO.setSagaId(sagaId);

            } finally {
                // Clean up
                sagaLatches.remove(sagaId);
                sagaResponses.remove(sagaId);
            }

        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for saga events", e);
            Thread.currentThread().interrupt();
            throw new ServiceException("Saga processing interrupted", "500", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error processing order", e);
            throw new RuntimeException(e);
        }
        return orchestratorResponseDTO;
    }

    @Transactional
    public OrchestratorResponseDTO reverseOrder(String orderId) {
        OrchestratorResponseDTO orchestratorResponseDTO = new OrchestratorResponseDTO();
        try {
            //Release Inventory
            //Reverse Order
            //Reverse Payment
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return orchestratorResponseDTO;
    }

    /**
     * Kafka listener to receive saga status updates from the saga-topic
     */
    @KafkaListener(topics = "${spring.kafka.template.saga-topic}", groupId = "orchestrator-group")
    public void handleSagaEvent(Saga sagaEvent) {
        log.info("Received saga event: {}", sagaEvent);

        Long sagaId = sagaEvent.getSagaId();
        // Store the saga response
        sagaResponses.put(sagaId, sagaEvent);

        // Signal that we've received a response for this saga
        CountDownLatch latch = sagaLatches.get(sagaId);
        if (latch != null) {
            latch.countDown();
            log.info("Saga event received for sagaId: {}", sagaId);
        }
    }
}
