package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.domain.enums.Direction;

public class UsePushFixedCommandPayload {
    private CommandType type;
    private int rowOrColIndex;
    private Direction direction;

    public UsePushFixedCommandPayload(int rowOrColIndex, Direction direction) {
        this.type = CommandType.USE_PUSH_FIXED;
        this.rowOrColIndex = rowOrColIndex;
        this.direction = direction;
    }

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
