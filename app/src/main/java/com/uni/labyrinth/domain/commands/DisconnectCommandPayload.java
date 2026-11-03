package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;

import static com.uni.labyrinth.domain.enums.CommandType.DISCONNECT;

public class DisconnectCommandPayload {


    private final String type = String.valueOf(CommandType.DISCONNECT);

    public DisconnectCommandPayload() {
    }

    public String getType() {
        return type;
    }
}
