package dev.lydtech.lib;

import dev.lydtech.exception.CtfExampleException;
import dev.lydtech.properties.CtfExampleProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaClient {
    @Autowired
    private CtfExampleProperties properties;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public SendResult sendMessage(String key, String data) {
        try {
            String payload = "payload: " + data;
            final ProducerRecord<String, String> record =
                    new ProducerRecord<>(properties.getOutboundTopic(), key, payload);

            final SendResult result = (SendResult) kafkaTemplate.send(record).get();
            final RecordMetadata metadata = result.getRecordMetadata();

            log.info(String.format("Sent record(key=%s value=%s) meta(topic=%s, partition=%d, offset=%d)",
                    record.key(), record.value(), metadata.topic(), metadata.partition(), metadata.offset()));

            return result;
        } catch (Exception e) {
            log.error("Error sending message to topic " + properties.getOutboundTopic(), e);
            throw new CtfExampleException(e);
        }
    }
}
