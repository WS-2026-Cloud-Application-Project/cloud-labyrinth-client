package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.model.*;

import java.util.List;

public class GameStateUpdatePayload {

    private String type;
    private GameBoard board;
    private List<PlayerState> players;
    private TurnInfo currentTurnInfo;
    private String gameEndTime;


    public GameBoard getBoard() {
        return board;
    }
    public Tile getExtraTile() {
        return board.getSpareTile();
    }
    public List<PlayerState> getPlayers() {
        return players;
    }
    //public String getCurrentPlayerId() { return currentPlayerId; }
    public TurnInfo getCurrentTurnInfo() {return currentTurnInfo;}
    public String getGameEndTime() {return gameEndTime;}
}
