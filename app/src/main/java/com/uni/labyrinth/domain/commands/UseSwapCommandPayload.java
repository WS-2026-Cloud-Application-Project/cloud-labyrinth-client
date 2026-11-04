package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;

import static com.uni.labyrinth.domain.enums.CommandType.USE_SWAP;

public class UseSwapCommandPayload {
    private CommandType type;
    private String targetPlayerId;

    public UseSwapCommandPayload(String targetPlayerId) {
        this.type = USE_SWAP;
        this.targetPlayerId = targetPlayerId;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public String getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }
}
