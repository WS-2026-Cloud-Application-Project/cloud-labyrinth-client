package com.uni.labyrinth.domain.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectCommandPayloadTest {

    @Test
    void getType() {
        ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", "andre");
        assertEquals("CONNECT", payload.getType());
    }

    @Test
    void setType() {
        ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", "andre");
        payload.setType("DISCONNECT");
        assertEquals("DISCONNECT", payload.getType());
    }

    @Test
    void getUsername() {
        ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", "andre");
        assertEquals("andre", payload.getUsername());
    }

    @Test
    void setUsername() {
        ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", "andre");
        payload.setUsername("max");
        assertEquals("max", payload.getUsername());
    }

    @Test
    void getIdentifierToken() {
        ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", "andre", "token123");
        assertEquals("token123", payload.getIdentifierToken());

        // optional: check constructor without token sets it to null
        ConnectCommandPayload payload2 = new ConnectCommandPayload("CONNECT", "andre");
        assertNull(payload2.getIdentifierToken());
    }

    @Test
    void setIdentifierToken() {
        ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", "andre");
        payload.setIdentifierToken("abc123");
        assertEquals("abc123", payload.getIdentifierToken());
    }
}
