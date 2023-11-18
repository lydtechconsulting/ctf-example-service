package dev.lydtech.component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import dev.lydtech.component.framework.client.kafka.KafkaClient;
import dev.lydtech.component.framework.client.wiremock.RequestCriteria;
import dev.lydtech.component.framework.client.wiremock.WiremockClient;
import dev.lydtech.component.framework.extension.ComponentTestExtension;
import dev.lydtech.component.framework.mapper.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.lydtech.component.TestEventData.INBOUND_DATA;
import static dev.lydtech.component.TestEventData.buildCtfExampleInboundEvent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Demonstrates the Kafka Consumer and Producer API.
 */
@Slf4j
@ExtendWith(ComponentTestExtension.class)
public class ConsumeAndProduceCT {

    private static final String GROUP_ID = "ConsumeAndProduceCT";
    private final static String INBOUND_TOPIC = "ctf-example-inbound-topic";
    private final static String OUTBOUND_TOPIC = "ctf-example-outbound-topic";
    private Consumer consumer;

    @BeforeEach
    public void setup() {
        WiremockClient.getInstance().deleteAllRequestsMappings();
        consumer = KafkaClient.getInstance().createConsumer(GROUP_ID, OUTBOUND_TOPIC);
        // Clear the topic.
        consumer.poll(Duration.ofSeconds(1));
    }

    @AfterEach
    public void tearDown() {
        consumer.close();
    }

    /**
     * An event is sent to the inbound topic for the service.
     *
     * The service consumes the event, calls a thirdparty API via REST, and produces a resulting event.
     *
     * The test consumes the outbound event and asserts it is as expected.
     *
     * This test therefore verifies Kafka and Wiremock containers are working as required.
     */
    @Test
    public void testConsumeAndProduce() throws Exception {
        String key = UUID.randomUUID().toString();
        String payload = UUID.randomUUID().toString();
        KafkaClient.getInstance().sendMessage(INBOUND_TOPIC, key, JsonMapper.writeToJson(buildCtfExampleInboundEvent(payload)));

        List<ConsumerRecord<String, String>> outboundEvents = KafkaClient.getInstance().consumeAndAssert("testConsumeAndProduce", consumer, 1, 2);
        assertThat(outboundEvents.get(0).value(), containsString(INBOUND_DATA));

        RequestCriteria request = RequestCriteria.builder()
                .method("GET")
                .url("/api/thirdparty/"+key)
                .build();
        WiremockClient.getInstance().countMatchingRequests(request, 1);
    }
}
