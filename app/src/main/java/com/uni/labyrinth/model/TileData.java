package com.uni.labyrinth.model;

import java.util.List;

public class TileData {

    private List<String> entrances;
    private Treasure treasure;
    private Bonus bonus;
    private boolean isFixed;

    public List<String> getEntrances() {
        return entrances;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public boolean isFixed() {
        return isFixed;
    }
}
