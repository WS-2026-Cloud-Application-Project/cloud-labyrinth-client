package com.uni.labyrinth.model;


import java.util.List;
import java.util.Map;
import java.util.Objects;


public class GameBoard {


    private int rows;
    private int cols;
    private List<List<Tile>> tiles;


    private Tile spareTile;
    private LastPush lastPush;
    private List <PlayerState> players;
    private Map<String, Object> additionalProperties;

    public int getRows() {
        return rows;
    }
    public int getCols() {
        return cols;
    }
    public List<List<Tile>> getTiles() {
        return tiles;
    }
    public Tile getSpareTile() { return spareTile; }
    public LastPush getLastPush() {
        return lastPush;
    }


    public Map<String, Object> getAdditionalProperties() { return this.additionalProperties; }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameBoard self = (GameBoard) o;
        return
                Objects.equals(this.rows, self.rows) &&
                        Objects.equals(this.cols, self.cols) &&
                        Objects.equals(this.tiles, self.tiles) &&
                        Objects.equals(this.spareTile, self.spareTile) &&
                        Objects.equals(this.lastPush, self.lastPush) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)rows, (Object)cols, (Object)tiles, (Object)spareTile, (Object)lastPush, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class GameBoard {\n" +
                "    rows: " + toIndentedString(rows) + "\n" +
                "    cols: " + toIndentedString(cols) + "\n" +
                "    tiles: " + toIndentedString(tiles) + "\n" +
                "    spareTile: " + toIndentedString(spareTile) + "\n" +
                "    lastPush: " + toIndentedString(lastPush) + "\n" +
                "    additionalProperties: " + toIndentedString(additionalProperties) + "\n" +
                "}";
    }


    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }



}
