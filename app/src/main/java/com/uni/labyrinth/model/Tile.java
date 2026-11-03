package com.uni.labyrinth.model;

import com.uni.labyrinth.domain.enums.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Tile {

    private final List<Direction> entrances;

    private final Treasure treasure;
    private final String bonus;
    private final boolean isFixed;
    private Map<String, Object> additionalProperties;

    public Tile() {
        this.entrances = new ArrayList<>(); // leere Liste statt null
        this.isFixed = false;               // Default-Wert
        this.treasure = null;               // optional
        this.bonus = null;                  // optional
    }

    public Tile(
            List<Direction> entrances,
            boolean isFixed,
            Treasure treasure,
            String bonus
    ) {
        this.entrances = entrances;
        this.isFixed = isFixed;
        this.treasure = treasure;
        this.bonus = bonus;
    }

    public List<Direction> getEntrances() {
        return entrances;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public String getBonus() {
        return bonus;
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
        Tile self = (Tile) o;
        return
                Objects.equals(this.entrances, self.entrances) &&
                        Objects.equals(this.treasure, self.treasure) &&
                        Objects.equals(this.bonus, self.bonus) &&
                        Objects.equals(this.isFixed, self.isFixed) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)entrances, (Object)treasure, (Object)bonus, (Object)isFixed, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class Tile {\n" +
                "    entrances: " + toIndentedString(entrances) + "\n" +
                "    treasure: " + toIndentedString(treasure) + "\n" +
                "    bonus: " + toIndentedString(bonus) + "\n" +
                "    isFixed: " + toIndentedString(isFixed) + "\n" +
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
