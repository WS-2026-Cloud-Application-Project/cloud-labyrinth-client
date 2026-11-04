package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;

public class RotateTileCommandPayload {
    private CommandType type; // CommandType als String (z.B. "CONNECT")

    public RotateTileCommandPayload() {
        this.type = CommandType.ROTATE_TILE;
    }

    // Getter & Setter
    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }
}
