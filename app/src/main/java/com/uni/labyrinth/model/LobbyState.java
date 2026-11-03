package com.uni.labyrinth.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LobbyState {

    private List<PlayerInfo> players = new ArrayList<>();
    private Runnable onUpdate;
    private String myPlayerID;
    private String identifierToken;

    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
        if (onUpdate != null) {
            onUpdate.run();
        }
    }

    public PlayerInfo getMyPlayer() {
        if (myPlayerID == null || players == null) {
            return null;
        }

        return players.stream()
                .filter(player -> myPlayerID.equals(player.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void setMyPlayerId(String myPlayerID) {
        this.myPlayerID = myPlayerID;
    }

    public void setIdentifierToken(String identifierToken) {
        this.identifierToken = identifierToken;
    }

    public String getMyPlayerId() {
        return myPlayerID;
    }

    public String getIdentifierToken() {
        return identifierToken;
    }

    public void setOnUpdate(Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public int getCurrentPlayers() {
        if (players == null) return 0;

        return players.size();
    }

    public int getMaxPlayers() {
        return 4;
    }

    public void setMyPlayer(PlayerInfo me) {
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LobbyState self = (LobbyState) o;
        return
                Objects.equals(this.players, self.players);
    }


    @Override
    public String toString() {
        return "class LobbyState {\n" +
                "    players: " + toIndentedString(players) + "\n" +
                "    myPlayerId: " + toIndentedString(myPlayerID) + "\n" +
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
