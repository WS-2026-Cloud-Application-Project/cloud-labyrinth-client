package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.model.*;

import java.util.List;

public class PlayerUpdateEventPayload {
    private String type;
    private PlayerInfo player;

    public String getType() {
        return type;
    }
    public PlayerInfo getPlayer() {
        return player;
    }
}
