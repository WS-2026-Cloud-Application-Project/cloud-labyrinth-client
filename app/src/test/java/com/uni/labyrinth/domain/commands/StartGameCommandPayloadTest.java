package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.model.BoardSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StartGameCommandPayloadTest {

    @Test
    void defaultConstructor_shouldSetFieldsToDefaultValues() {
        StartGameCommandPayload payload = new StartGameCommandPayload();

        assertNull(payload.getType());
        assertNull(payload.getBoardSize());
        assertEquals(0, payload.getTreasureCardCount());
    }

    @Test
    void constructor_shouldSetAllGivenValues() {
        BoardSize boardSize = new BoardSize();
        boardSize.setRows(7);
        boardSize.setCols(7);

        StartGameCommandPayload payload = new StartGameCommandPayload(
                CommandType.START_GAME,  // falls es den Enum-Wert gibt
                boardSize,
                10,
                300,
                10
        );

        assertEquals(CommandType.START_GAME, payload.getType());
        assertEquals(boardSize, payload.getBoardSize()); // nutzt equals() von BoardSize
        assertEquals(10, payload.getTreasureCardCount());
    }

    @Test
    void setType() {
        StartGameCommandPayload payload = new StartGameCommandPayload();

        payload.setType(CommandType.START_GAME);
        assertEquals(CommandType.START_GAME, payload.getType());
    }

    @Test
    void setBoardSize() {
        StartGameCommandPayload payload = new StartGameCommandPayload();

        BoardSize boardSize = new BoardSize();
        boardSize.setRows(5);
        boardSize.setCols(9);

        payload.setBoardSize(boardSize);

        assertEquals(boardSize, payload.getBoardSize());
        assertEquals(5, payload.getBoardSize().getRows());
        assertEquals(9, payload.getBoardSize().getCols());
    }

    @Test
    void setTreasureCardCount() {
        StartGameCommandPayload payload = new StartGameCommandPayload();

        payload.setTreasureCardCount(15);
        assertEquals(15, payload.getTreasureCardCount());
    }
}
