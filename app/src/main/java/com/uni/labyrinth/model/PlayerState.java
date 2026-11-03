package com.uni.labyrinth.model;


import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.domain.enums.BonusType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerState {
    private PlayerInfo playerInfo;
    private Coordinates currentPosition;
    private Coordinates homePosition;
    private List<Treasure> treasuresFound;
    private Treasure currentTreasure;
    private int remainingTreasureCount;
    private List<AchievementType> availableAchievements;
    private List<BonusType> availableBonuses;
    private Map<String, Object> additionalProperties;

    // Standard-Konstruktor (für Gson)
    public  PlayerState() {}

    // Getter
    public PlayerInfo getPlayerInfo() { return playerInfo; }
    public Coordinates getCurrentPosition() { return currentPosition; }
    public Coordinates getHomePosition() { return homePosition; }
    public List<Treasure> getTreasuresFound() { return treasuresFound; }
    public Treasure getCurrentTreasure() { return currentTreasure; }
    public int getRemainingTreasureCount() { return remainingTreasureCount; }
    public int getTreasuresFoundCount() {
        return treasuresFound != null ? treasuresFound.size() : 0;
    }
    public List<BonusType> getAvailableBonuses() { return availableBonuses; }
    public List<AchievementType> getAvailableAchievements() { return availableAchievements; }

    // Setter
    public void setPlayer(PlayerInfo player) { this.playerInfo = player; }
    public void setCurrentPosition(Coordinates currentPosition) { this.currentPosition = currentPosition; }
    public void setHomePosition(Coordinates homePosition) { this.homePosition = homePosition; }
    public void setTreasuresFound(List<Treasure> treasuresFound) { this.treasuresFound = treasuresFound; }
    public void setCurrentTreasure(int id, String name ) {
        this.currentTreasure= new Treasure();
        this.currentTreasure.setId(id);
        this.currentTreasure.setName(name);
    }
    public void setRemainingTreasureCount(int remainingTreasureCount) { this.remainingTreasureCount = remainingTreasureCount; }
    public void setAvailableBonuses(List<BonusType> availableBonuses) { this.availableBonuses = availableBonuses; }
    public void setAvailableAchievements(List<AchievementType> availableAchievements) { this.availableAchievements = availableAchievements; }

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
        PlayerState self = (PlayerState) o;
        return
                Objects.equals(this.playerInfo, self.playerInfo) &&
                        Objects.equals(this.currentPosition, self.currentPosition) &&
                        Objects.equals(this.homePosition, self.homePosition) &&
                        Objects.equals(this.treasuresFound, self.treasuresFound) &&
                        Objects.equals(this.remainingTreasureCount, self.remainingTreasureCount) &&
                        Objects.equals(this.availableAchievements, self.availableAchievements) &&
                        Objects.equals(this.availableBonuses, self.availableBonuses) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)playerInfo, (Object)currentPosition, (Object)homePosition, (Object)treasuresFound, (Object)remainingTreasureCount, (Object)availableAchievements, (Object)availableBonuses, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class PlayerState {\n" +
                "    playerInfo: " + toIndentedString(playerInfo) + "\n" +
                "    currentPosition: " + toIndentedString(currentPosition) + "\n" +
                "    homePosition: " + toIndentedString(homePosition) + "\n" +
                "    treasuresFound: " + toIndentedString(treasuresFound) + "\n" +
                "    remainingTreasureCount: " + toIndentedString(remainingTreasureCount) + "\n" +
                "    achievements: " + toIndentedString(availableAchievements) + "\n" +
                "    availableBonuses: " + toIndentedString(availableBonuses) + "\n" +
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
