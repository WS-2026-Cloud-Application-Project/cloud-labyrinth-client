package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.domain.enums.Direction;

public class PushTileCommandPayload {

    private CommandType type;      // PUSH_TILE
    private int rowOrColIndex;     // Zeile oder Spalte
    private Direction direction;   // UP, DOWN, LEFT, RIGHT

    public PushTileCommandPayload(int rowOrColIndex, Direction direction) {
        this.type = CommandType.PUSH_TILE;
        this.rowOrColIndex = rowOrColIndex;
        this.direction = direction;
    }

    // Getter und Setter
    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public int getRowOrColIndex() {
        return rowOrColIndex;
    }

    public void setRowOrColIndex(int rowOrColIndex) {
        this.rowOrColIndex = rowOrColIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
