package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.model.Coordinates;

public class MovePawnCommandPayload {

    private CommandType type;      // PUSH_TILE
    private Coordinates targetCoordinates;     // Zeile oder Spalte

    public MovePawnCommandPayload() {
    }

    public MovePawnCommandPayload(CommandType type, Coordinates coordinates) {
        this.type = type;
        this.targetCoordinates = coordinates;
    }

    // Getter und Setter
    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public Coordinates getTargetCoordinates() {
        return targetCoordinates;
    }

    public void setTargetCoordinates(Coordinates targetCoordinates) {
        this.targetCoordinates = targetCoordinates;
    }

    // Legacy methods for backward compatibility
    @Deprecated
    public Coordinates getCoordinates() {
        return targetCoordinates;
    }

    @Deprecated
    public void setCoordinates(Coordinates coordinates) {
        this.targetCoordinates = coordinates;
    }
}
