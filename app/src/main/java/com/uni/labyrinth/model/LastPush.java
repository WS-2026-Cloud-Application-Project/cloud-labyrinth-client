package com.uni.labyrinth.model;

import com.uni.labyrinth.domain.enums.Direction;

public class LastPush {

    // WICHTIG: Feldname muss mit Server-JSON übereinstimmen: "rowOrColIndex"
    private int rowOrColIndex;
    private Direction direction;

    // Default-Konstruktor für Gson
    public LastPush() {}

    public LastPush(int rowOrColIndex, Direction direction) {
        this.rowOrColIndex = rowOrColIndex;
        this.direction = direction;
    }

    public int getRowOrColumnIndex() {return rowOrColIndex;}
    public Direction getDirection() {return direction;}
}
