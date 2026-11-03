package com.uni.labyrinth.model;


import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

public class PlayerInfo {

    private String id;
    private String identifierToken;  // Stays the same on reconnect, while id changes!
    private String name;
    private String color;
    private boolean isAdmin;
    private boolean isAiControlled;
    private boolean isConnected;
    private Map<String, Object> additionalProperties;



    // Standard-Konstruktor (für Gson)
    public PlayerInfo() {}

    // Voller Konstruktor
    public PlayerInfo(String id, String name, String color, boolean isAdmin, boolean isAiControlled,boolean isConnected) {
        this.id = id;
        this.identifierToken = id; // Default: same as id
        this.name = name;
        this.color = color;
        this.isAdmin = isAdmin;
        this.isAiControlled = isAiControlled;
        this.isConnected=isConnected;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdentifierToken() { return identifierToken; }
    public void setIdentifierToken(String identifierToken) { this.identifierToken = identifierToken; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean getIsAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public boolean getIsAiControlled() { return isAiControlled; }
    public void setAiControlled(boolean aiControlled) { this.isAiControlled = aiControlled; }

    public boolean getIsConnected() { return isConnected; }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerInfo self = (PlayerInfo) o;
        return
                Objects.equals(this.id, self.id) &&
                        Objects.equals(this.name, self.name) &&
                        Objects.equals(this.color, self.color) &&
                        Objects.equals(this.isAdmin, self.isAdmin) &&
                        Objects.equals(this.isAiControlled, self.isAiControlled) &&
                        Objects.equals(this.isConnected, self.isConnected) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)id, (Object)name, (Object)color, (Object)isAdmin, (Object)isAiControlled, (Object)isConnected, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class PlayerInfo {\n" +
                "    id: " + toIndentedString(id) + "\n" +
                "    name: " + toIndentedString(name) + "\n" +
                "    color: " + toIndentedString(color) + "\n" +
                "    isAdmin: " + toIndentedString(isAdmin) + "\n" +
                "    isAiControlled: " + toIndentedString(isAiControlled) + "\n" +
                "    isConnected: " + toIndentedString(isConnected) + "\n" +
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
