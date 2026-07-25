package org.project.paymentservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "my-topic", groupId = "payment-service-group")
    public void consume(String message) {
        System.out.println("Message received: " + message);
    }
}
