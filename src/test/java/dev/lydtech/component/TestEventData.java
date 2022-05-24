package dev.lydtech.component;

import dev.lydtech.event.CtfExampleInboundEvent;

public class TestEventData {

    public static String INBOUND_DATA = "event data";

    public static CtfExampleInboundEvent buildCtfExampleInboundEvent(String id) {
        return CtfExampleInboundEvent.builder()
                .id(id)
                .data(INBOUND_DATA)
                .build();
    }
}
