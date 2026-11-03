package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.model.Tile;

public class PlayerTurnPayload {
    private String type; // CommandType als String (z.B. "CONNECT")
    private String playerId;
    private Tile extraTile;

    // Getter & Setter
    public String getType() {
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Tile getExtraTile() {
        return extraTile;
    }
}
