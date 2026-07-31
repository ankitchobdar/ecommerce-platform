package org.project.orchestrator.service;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.project.common.order.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(@NotNull String topic, String key, @NotNull Object message) {
        log.info("send message topic:{} key:{} message:{}", topic, key, message);
        kafkaTemplate.send(topic, key, message);
    }
}
