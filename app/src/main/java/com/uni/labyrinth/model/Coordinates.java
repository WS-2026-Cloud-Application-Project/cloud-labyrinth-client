package com.uni.labyrinth.model;

import java.util.Map;
import java.util.Objects;

public class Coordinates {
    public  int column;
    public  int row;
    private Map<String, Object> additionalProperties;

    public Coordinates() {   }

    public Coordinates(int column, int row) {
        this.column = column;
        this.row = row;
    }
    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setColumn(int c) {
        if (c < 0) {
            throw new IllegalArgumentException("Column coordinate cannot be negative");
        }
        this.column = c;
    }
    public void setRow(int r) {
        if (r < 0) {
            throw new IllegalArgumentException("Row coordinate cannot be negative");
        }
        this.row = r;
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
        Coordinates self = (Coordinates) o;
        return
                Objects.equals(this.row, self.row) &&
                        Objects.equals(this.column, self.column) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)row, (Object)column, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class Coordinates {\n" +
                "    row: " + toIndentedString(row) + "\n" +
                "    column: " + toIndentedString(column) + "\n" +
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