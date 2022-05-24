package dev.lydtech.service;

import java.util.UUID;

import dev.lydtech.domain.OutboxEvent;
import dev.lydtech.event.CtfExampleInboundEvent;
import dev.lydtech.exception.CtfExampleException;
import dev.lydtech.lib.KafkaClient;
import dev.lydtech.properties.CtfExampleProperties;
import dev.lydtech.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class CtfExampleService {

    @Autowired
    private CtfExampleProperties properties;

    @Autowired
    private KafkaClient kafkaClient;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    /**
     * Sends an outbound event in response to the received event.
     */
    public void process(String key, CtfExampleInboundEvent event) {
//        callThirdparty(key);
        kafkaClient.sendMessage(key, event.getData());
    }

    /**
     * Writes an event to the outbox in response to the received event.
     *
     * This will be picked up by Kafka Connect and published to the topic.
     */
    public void processWithOutbox(String key, CtfExampleInboundEvent event) {
//        callThirdparty(key);
        writeOutboxEvent(event.getData());
    }

    private void callThirdparty(String key) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(properties.getThirdpartyEndpoint() + "/" + key, String.class);
            if (response.getStatusCodeValue() != 200) {
                throw new RuntimeException("error " + response.getStatusCodeValue());
            }
            return;
        } catch (Exception e) {
            log.error("Error calling thirdparty api, returned an (" + e.getClass().getName() + ")", e);
            throw new CtfExampleException(e);
        }
    }

    private void writeOutboxEvent(String payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .version("v1")
                .payload(payload)
                .destination(properties.getOutBoxOutboundTopic())
                .timestamp(System.currentTimeMillis())
                .build();
        UUID outboxEventId = outboxEventRepository.save(outboxEvent).getId();
        log.debug("Event persisted to transactional outbox with Id: {}", outboxEventId);
    }
}
