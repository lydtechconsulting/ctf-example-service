package dev.lydtech.component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import dev.lydtech.component.framework.client.debezium.DebeziumClient;
import dev.lydtech.component.framework.client.kafka.KafkaClient;
import dev.lydtech.component.framework.client.wiremock.RequestCriteria;
import dev.lydtech.component.framework.client.wiremock.WiremockClient;
import dev.lydtech.component.framework.extension.TestContainersSetupExtension;
import dev.lydtech.component.framework.mapper.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

import static dev.lydtech.component.TestEventData.INBOUND_DATA;
import static dev.lydtech.component.TestEventData.buildCtfExampleInboundEvent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Demonstrates the Transactional Outbox pattern.
 */
@Slf4j
@ExtendWith(TestContainersSetupExtension.class)
@ActiveProfiles("component-test")
public class TransactionalOutboxCT {

    private static final String GROUP_ID = "TransactionalOutboxCT";
    private final static String INBOUND_TOPIC = "ctf-example-inbound-with-outbox-topic";
    private final static String OUTBOUND_TOPIC = "ctf-example-server.ctf_example.outbox_event";

    private Consumer consumer;

    @BeforeEach
    public void setup() {
        consumer = KafkaClient.getInstance().createConsumer(GROUP_ID, OUTBOUND_TOPIC);
        DebeziumClient.getInstance().createConnector("connector/outbox-connector.json");
        // Clear the topic.
        consumer.poll(Duration.ofSeconds(1));
    }

    @AfterEach
    public void tearDown() {
        DebeziumClient.getInstance().deleteConnector("outbox-connector");
        consumer.close();
    }

    /**
     * An event is sent to the inbound topic that uses the transactional outbox for the service.
     *
     * The service consumes the event, calls a thirdparty API via REST, and writes an outbox event to the database.
     *
     * Debezium (Kafka Connect source connector) streams the database log write to the Kafka outbound topic.
     *
     * The test consumes the outbound event and asserts it is as expected.
     *
     * This test therefore verifies Kafka, Wiremock, Postgres and Debezium containers are working as required.
     */
    @Test
    public void testTransactionalOutbox() throws Exception {
        String key = UUID.randomUUID().toString();
        String payload = UUID.randomUUID().toString();
        KafkaClient.getInstance().sendMessage(INBOUND_TOPIC, key, JsonMapper.writeToJson(buildCtfExampleInboundEvent(payload)));

        List<ConsumerRecord<String, String>> outboundEvents = KafkaClient.getInstance().consumeAndAssert("testTransactionalOutbox", consumer, 1, 2);
        assertThat(outboundEvents.get(0).value(), containsString(INBOUND_DATA));

        RequestCriteria request = RequestCriteria.builder()
                .method("GET")
                .url("/api/thirdparty/"+key)
                .build();
        WiremockClient.getInstance().countMatchingRequests(request, 1);
    }
}
