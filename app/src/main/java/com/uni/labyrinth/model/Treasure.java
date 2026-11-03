package com.uni.labyrinth.model;

public class Treasure {

    public Treasure() {}
    public Treasure(int id, String name) {
        this.id = id;
        this.name = name;
    };
    private int id;
    private String name;

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
