package com.uni.labyrinth.model;

public class PlayerGameStats {
    private int stepsTaken;
    private int tilesPushed;
    private int treasuresCollected;

    public PlayerGameStats() {
        this.stepsTaken = 0;
        this.tilesPushed = 0;
        this.treasuresCollected = 0;
    }

    public PlayerGameStats(int stepsTaken, int tilesPushed, int treasuresCollected) {
        this.stepsTaken = stepsTaken;
        this.tilesPushed = tilesPushed;
        this.treasuresCollected = treasuresCollected;
    }

    public int getStepsTaken() {
        return stepsTaken;
    }

    public void setStepsTaken(int stepsTaken) {
        this.stepsTaken = stepsTaken;
    }

    public int getTilesPushed() {
        return tilesPushed;
    }

    public void setTilesPushed(int tilesPushed) {
        this.tilesPushed = tilesPushed;
    }

    public int getTreasuresCollected() {
        return treasuresCollected;
    }

    public void setTreasuresCollected(int treasuresCollected) {
        this.treasuresCollected = treasuresCollected;
    }
}
