package org.project.paymentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.project.common.order.Order;
import org.project.common.payment.PaymentDTO;
import org.project.common.utility.MessageUtility;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

//    @KafkaListener(topics = "my-topic", groupId = "payment-service-group")
//    public PaymentDTO consume(String message) {
//        log.info("Message received: {}", message);
//        return new PaymentDTO(
//                MessageUtility.getBaseMessage(org.project.common.Status.SUCCESS, "Message received "+message),
//                -1L,
//                org.project.common.Status.COMPLETED);
//    }

    @KafkaListener(topics = "my-topic", groupId = "payment-service-group")
    public PaymentDTO consume(Order order) {
        log.info("Message received: {}", order);
        return new PaymentDTO(
                MessageUtility.getBaseMessage(org.project.common.Status.SUCCESS, "Message received "+order),
                -1L,
                org.project.common.Status.COMPLETED);
    }
}
