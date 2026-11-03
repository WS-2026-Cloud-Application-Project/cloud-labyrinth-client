package com.uni.labyrinth.model;

import com.google.gson.annotations.SerializedName;
import com.uni.labyrinth.domain.enums.TurnState;

import java.util.Map;
import java.util.Objects;

public class TurnInfo {
    private String currentPlayerId;
    private String turnEndTime;
    // Server sends "turnState", client uses "state"
    @SerializedName(value = "state", alternate = {"turnState"})
    private TurnState state;
    private Map<String, Object> additionalProperties;
    // Default-Konstruktor für Gson
    public TurnInfo() {}

    public TurnInfo(String currentPlayerId, String turnEndTime, TurnState state) {
        this.currentPlayerId = currentPlayerId;
        this.turnEndTime = turnEndTime;
        this.state = state;
        if(this.state==null){
            this.state= TurnState.WAITING_FOR_PUSH;
        }
    }
    public String getCurrentPlayerId() {
        return currentPlayerId;
    }
    public String getTurnEndTime() {
        return turnEndTime;
    }
    public TurnState getState() {
        return state;
    }
    public void setTurnEndTime(String turnEndTime) {
        this.turnEndTime = turnEndTime;
    }

    public void setState(TurnState turnState) {
        this.state = turnState;  // Fixed: was assigning state to itself
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
        TurnInfo self = (TurnInfo) o;
        return
                Objects.equals(this.currentPlayerId, self.currentPlayerId) &&
                        Objects.equals(this.turnEndTime, self.turnEndTime) &&
                        Objects.equals(this.state, self.state) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)currentPlayerId, (Object)turnEndTime, (Object)state, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class CurrentTurnInfo {\n" +
                "    currentPlayerId: " + toIndentedString(currentPlayerId) + "\n" +
                "    turnEndTime: " + toIndentedString(turnEndTime) + "\n" +
                "    state: " + toIndentedString(state) + "\n" +
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
