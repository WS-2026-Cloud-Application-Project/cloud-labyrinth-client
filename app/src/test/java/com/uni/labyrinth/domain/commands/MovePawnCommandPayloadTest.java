package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.model.Coordinates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovePawnCommandPayloadTest {

    @Test
    void getType() {
        Coordinates coordinates = new Coordinates(1, 2);
        MovePawnCommandPayload payload = new MovePawnCommandPayload(CommandType.PUSH_TILE, coordinates);

        assertEquals(CommandType.PUSH_TILE, payload.getType());
    }

    @Test
    void setType() {
        MovePawnCommandPayload payload = new MovePawnCommandPayload();
        payload.setType(CommandType.PUSH_TILE);

        assertEquals(CommandType.PUSH_TILE, payload.getType());
    }

    @Test
    void getCoordinates() {
        Coordinates coordinates = new Coordinates(3, 4);
        MovePawnCommandPayload payload = new MovePawnCommandPayload(CommandType.PUSH_TILE, coordinates);

        assertEquals(coordinates, payload.getCoordinates());
    }

    @Test
    void setCoordinates() {
        MovePawnCommandPayload payload = new MovePawnCommandPayload();
        Coordinates coordinates = new Coordinates(5, 6);

        payload.setCoordinates(coordinates);

        assertEquals(coordinates, payload.getCoordinates());
    }

    @Test
    void defaultConstructor_shouldSetFieldsToNull() {
        MovePawnCommandPayload payload = new MovePawnCommandPayload();

        assertNull(payload.getType());
        assertNull(payload.getCoordinates());
    }
}
