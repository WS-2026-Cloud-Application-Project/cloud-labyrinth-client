package com.uni.labyrinth.domain.events;

import com.google.gson.annotations.SerializedName;
import com.uni.labyrinth.model.GameBoard;
import com.uni.labyrinth.model.PlayerState;
import com.uni.labyrinth.model.TurnInfo;

import java.util.List;

public class GameStartedPayload {

    private String type;
    private GameBoard board;
    private List<PlayerState> players;
    // Server sends "turnInfo" in GAME_STARTED, but "currentTurnInfo" in GAME_STATE_UPDATE
    @SerializedName(value = "currentTurnInfo", alternate = {"turnInfo"})
    private TurnInfo currentTurnInfo;
    private String gameEndTime ;


    public String getType() {
        return type;
    }
    public GameBoard getBoard() {
        return board;
    }
    public List<PlayerState> getPlayers() {
        return players;
    }
    public TurnInfo getCurrentTurnInfo() { return currentTurnInfo; }
    public String getGameEndTime() { return gameEndTime; }
}
