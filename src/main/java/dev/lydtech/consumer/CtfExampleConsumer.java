package dev.lydtech.consumer;

import java.util.concurrent.atomic.AtomicInteger;

import dev.lydtech.event.CtfExampleInboundEvent;
import dev.lydtech.mapper.JsonMapper;
import dev.lydtech.service.CtfExampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CtfExampleConsumer {

    final AtomicInteger counter = new AtomicInteger();
    final CtfExampleService ctfExampleService;

    @KafkaListener(topics = "ctf-example-inbound-topic", groupId = "kafkaConsumerGroup", containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload final String payload) {
        counter.getAndIncrement();
        log.debug("Received message [" +counter.get()+ "] - key: " + key + " - payload: " + payload);
        try {
            CtfExampleInboundEvent event = JsonMapper.readFromJson(payload, CtfExampleInboundEvent.class);
            ctfExampleService.process(key, event);
        } catch (Exception e) {
            log.error("Dead message handler: Error processing message: " + e.getMessage());
        }
    }
}
