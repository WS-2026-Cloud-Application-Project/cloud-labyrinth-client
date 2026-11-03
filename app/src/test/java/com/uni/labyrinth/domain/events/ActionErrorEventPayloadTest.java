package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.domain.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionErrorEventPayloadTest {

    @Test
    void getType() {
        ActionErrorEventPayload payload = new ActionErrorEventPayload();
        payload.setType("ACTION_ERROR");

        assertEquals("ACTION_ERROR", payload.getType());
    }

    @Test
    void setType() {
        ActionErrorEventPayload payload = new ActionErrorEventPayload();
        payload.setType("ERROR");

        assertEquals("ERROR", payload.getType());
    }

    @Test
    void getErrorCode() {
        ActionErrorEventPayload payload = new ActionErrorEventPayload();
        payload.setErrorCode(ErrorCode.GENERAL);

        assertEquals(ErrorCode.GENERAL, payload.getErrorCode());
    }

    @Test
    void setErrorCode() {
        ActionErrorEventPayload payload = new ActionErrorEventPayload();
        payload.setErrorCode(ErrorCode.INVALID_MOVE);

        assertEquals(ErrorCode.INVALID_MOVE, payload.getErrorCode());
    }

    @Test
    void getMessage() {
        ActionErrorEventPayload payload = new ActionErrorEventPayload();
        payload.setMessage("Something went wrong");

        assertEquals("Something went wrong", payload.getMessage());
    }

    @Test
    void setMessage() {
        ActionErrorEventPayload payload = new ActionErrorEventPayload();
        payload.setMessage("Invalid move!");

        assertEquals("Invalid move!", payload.getMessage());
    }
}
