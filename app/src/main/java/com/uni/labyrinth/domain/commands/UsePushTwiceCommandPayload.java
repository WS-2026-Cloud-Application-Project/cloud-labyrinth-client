package com.uni.labyrinth.domain.commands;


import com.uni.labyrinth.domain.enums.CommandType;

import static com.uni.labyrinth.domain.enums.CommandType.USE_PUSH_TWICE;

public class UsePushTwiceCommandPayload {
    private CommandType type = USE_PUSH_TWICE;

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }
}