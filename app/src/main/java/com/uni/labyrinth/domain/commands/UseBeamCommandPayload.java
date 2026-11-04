package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.model.Coordinates;

import static com.uni.labyrinth.domain.enums.CommandType.USE_BEAM;

public class UseBeamCommandPayload {
    private CommandType type = USE_BEAM;

    private Coordinates targetCoordinates;     // Zeile oder Spalte

    public UseBeamCommandPayload(Coordinates coordinates) {
        this.targetCoordinates = coordinates;
    }

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
}
