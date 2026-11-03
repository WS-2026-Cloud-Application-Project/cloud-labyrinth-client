package com.uni.labyrinth.model;

import javafx.scene.paint.Color;

public class Pawn {
    private int x;
    private int y;
    private Color color; // z.B. für die Darstellung

    public Pawn(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public Color getColor() { return color; }
}
