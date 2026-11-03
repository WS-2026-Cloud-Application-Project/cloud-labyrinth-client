package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RotateTileCommandPayloadTest {

    @Test
    void defaultConstructor_shouldSetTypeToRotateTile() {
        RotateTileCommandPayload payload = new RotateTileCommandPayload();

        assertEquals(CommandType.ROTATE_TILE, payload.getType());
    }

    @Test
    void getType() {
        RotateTileCommandPayload payload = new RotateTileCommandPayload();

        assertEquals(CommandType.ROTATE_TILE, payload.getType());
    }

    @Test
    void setType() {
        RotateTileCommandPayload payload = new RotateTileCommandPayload();

        payload.setType(CommandType.PUSH_TILE); // falls es den Enum-Wert gibt
        assertEquals(CommandType.PUSH_TILE, payload.getType());
    }
}
