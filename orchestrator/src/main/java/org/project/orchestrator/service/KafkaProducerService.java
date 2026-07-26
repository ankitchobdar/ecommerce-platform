package org.project.orchestrator.service;

import lombok.extern.slf4j.Slf4j;
import org.project.common.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Order message) {
        log.info("send message topic:{} message:{}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}
