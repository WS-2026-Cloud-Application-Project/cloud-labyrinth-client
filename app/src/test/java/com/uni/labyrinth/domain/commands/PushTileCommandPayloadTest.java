package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.domain.enums.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushTileCommandPayloadTest {

    @Test
    void constructor_shouldSetTypeToPushTile() {
        PushTileCommandPayload payload = new PushTileCommandPayload(3, Direction.UP);

        assertEquals(CommandType.PUSH_TILE, payload.getType());
    }

    @Test
    void getType() {
        PushTileCommandPayload payload = new PushTileCommandPayload(1, Direction.LEFT);

        assertEquals(CommandType.PUSH_TILE, payload.getType());
    }

    @Test
    void setType() {
        PushTileCommandPayload payload = new PushTileCommandPayload(1, Direction.LEFT);

        payload.setType(CommandType.MOVE_PAWN); // falls es den Enum-Wert gibt
        assertEquals(CommandType.MOVE_PAWN, payload.getType());
    }

    @Test
    void getDirection() {
        PushTileCommandPayload payload = new PushTileCommandPayload(2, Direction.DOWN);

        assertEquals(Direction.DOWN, payload.getDirection());
    }

    @Test
    void setDirection() {
        PushTileCommandPayload payload = new PushTileCommandPayload(2, Direction.DOWN);

        payload.setDirection(Direction.RIGHT);
        assertEquals(Direction.RIGHT, payload.getDirection());
    }
}
