package com.uni.labyrinth.model;

import com.uni.labyrinth.domain.enums.TurnState;

import java.util.List;
import java.util.ArrayList;


public class GameState {

    private Tile[][] board = new Tile[0][0];
    private List<PlayerState> players = new ArrayList<>();
    private PlayerState currentPlayer = null;
    private String myPlayerId = "";
    private Tile extraTile = new Tile();
    private String currentPlayerId = "";
    private TurnState currentTurnState;
    private LastPush lastPush;
    private Treasure currentTreasure = new Treasure();
    private final List<GameStateListener> listeners = new ArrayList<>();

    // Default-Konstruktor
    public GameState() {
        // Felder sind schon initialisiert
    }

    // Getter / Setter
    public Tile[][] getBoard() { return board; }
    public void setBoard(Tile[][] board) { this.board = board; }

    public List<PlayerState> getPlayers() { return players; }
    public void setPlayers(List<PlayerState> players) {
        this.players = players;
        notifyListeners();
    }

    public PlayerState getMyPlayerState() {
        if (myPlayerId == null || players == null) {
            return null;
        }
        PlayerState   myPlayerState = players.stream()
                .filter(p -> p.getPlayerInfo() != null && myPlayerId.equals(p.getPlayerInfo().getId()))
                .findFirst()
                .orElse(null);
        if(currentTreasure != null) {
            myPlayerState.setCurrentTreasure(currentTreasure.getId(), currentTreasure.getName());
        }
        return myPlayerState;
    }

    public PlayerState getPlayerById(String playerId) {
        if(playerId == null) return null;

        // First, try to find by current player ID
        PlayerState byId = players.stream()
                .filter(p -> p.getPlayerInfo() != null && playerId.equals(p.getPlayerInfo().getId()))
                .findFirst()
                .orElse(null);

        if (byId != null) {
            return byId;
        }

        // If not found, try to find by identifierToken (for reconnected players)
        // The ranking uses old player IDs, but identifierToken stays the same
        return players.stream()
                .filter(p -> p.getPlayerInfo() != null && playerId.equals(p.getPlayerInfo().getIdentifierToken()))
                .findFirst()
                .orElse(null);
    }

    public PlayerState getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(PlayerState playerState) {
        this.currentPlayer = playerState;
        notifyListeners();
    }

    public Tile getExtraTile() { return extraTile; }
    public void setExtraTile(Tile extraTile) {
        this.extraTile = extraTile;
        notifyListeners();
    }

    public void setMyPlayerId(String myPlayerId) {
        this.myPlayerId = myPlayerId;
        notifyListeners();
    }

    public String getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
        notifyListeners();
    }

    public void setCurrentTurnState(TurnState currentTurnState) {
        this.currentTurnState = currentTurnState;
        notifyListeners();
    }
    public TurnState getCurrentTurnState() { return currentTurnState; }


     public void setCurrentTreasure(int id,String name){
         currentTreasure.setId(id);
         currentTreasure.setName(name);
         notifyListeners();

     }



    public LastPush getLastPush() { return lastPush; }
    public void setLastPush(LastPush lastPush) { this.lastPush = lastPush; }

    public void addListener(GameStateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (GameStateListener l : listeners) {
            l.onGameStateChanged(this);
        }
    }
    public void applyServerUpdate(Tile[][] board,
                                  Tile extraTile,
                                  TurnState currentTurnState,
                                  PlayerState currentPlayer,
                                  String currentPlayerId,
                                  List<PlayerState> players ) {
        this.board = board;
        this.extraTile = extraTile;
        this.currentTurnState = currentTurnState;
        this.currentPlayer =currentPlayer;
        this.currentPlayerId = currentPlayerId;

        // Merge player lists: Keep disconnected players instead of removing them
        mergePlayers(players);

        notifyListeners();
    }

    /**
     * Merges the incoming player list with the existing one.
     * - Updates existing players with new data (e.g., on reconnect)
     * - Adds new players
     * - Keeps disconnected players (not in the incoming list) for ResultScreen
     * - Prevents duplicates by matching players using identifierToken (player ID changes on reconnect!)
     */
    private void mergePlayers(List<PlayerState> incomingPlayers) {
        if (incomingPlayers == null) {
            return;
        }

        // If this is the first update, just set the list
        if (this.players == null || this.players.isEmpty()) {
            this.players = new ArrayList<>(incomingPlayers);
            return;
        }

        System.out.println("🔍 DEBUG mergePlayers:");
        System.out.println("   Existing players: " + this.players.size());
        for (PlayerState p : this.players) {
            String id = p.getPlayerInfo() != null ? p.getPlayerInfo().getId() : "null";
            String token = p.getPlayerInfo() != null ? p.getPlayerInfo().getIdentifierToken() : "null";
            String name = p.getPlayerInfo() != null ? p.getPlayerInfo().getName() : "null";
            System.out.println("      - " + name + " (ID: " + id + ", Token: " + token + ")");
        }
        System.out.println("   Incoming players: " + incomingPlayers.size());
        for (PlayerState p : incomingPlayers) {
            String id = p.getPlayerInfo() != null ? p.getPlayerInfo().getId() : "null";
            String token = p.getPlayerInfo() != null ? p.getPlayerInfo().getIdentifierToken() : "null";
            String name = p.getPlayerInfo() != null ? p.getPlayerInfo().getName() : "null";
            System.out.println("      - " + name + " (ID: " + id + ", Token: " + token + ")");
        }

        // Build a list of merged players
        List<PlayerState> mergedPlayers = new ArrayList<>();
        List<String> processedTokens = new ArrayList<>();  // Track which identifierTokens we've already processed

        // First, process all incoming players (these are currently connected)
        for (PlayerState incomingPlayer : incomingPlayers) {
            mergedPlayers.add(incomingPlayer);

            // Track this identifierToken as processed
            String incomingToken = incomingPlayer.getPlayerInfo() != null ?
                incomingPlayer.getPlayerInfo().getIdentifierToken() : null;
            if (incomingToken != null) {
                processedTokens.add(incomingToken);
            }
        }

        // Then, add disconnected players (those whose identifierToken is not in incoming list)
        for (PlayerState existingPlayer : this.players) {
            String existingToken = existingPlayer.getPlayerInfo() != null ?
                existingPlayer.getPlayerInfo().getIdentifierToken() : null;

            // If this token hasn't been processed yet, it's a disconnected player
            if (existingToken != null && !processedTokens.contains(existingToken)) {
                mergedPlayers.add(existingPlayer);
                System.out.println("   → Keeping disconnected player: " + existingPlayer.getPlayerInfo().getName() + " (Token: " + existingToken + ")");
            }
        }

        this.players = mergedPlayers;

        System.out.println("   Merged players: " + this.players.size());
        for (PlayerState p : this.players) {
            String id = p.getPlayerInfo() != null ? p.getPlayerInfo().getId() : "null";
            String token = p.getPlayerInfo() != null ? p.getPlayerInfo().getIdentifierToken() : "null";
            String name = p.getPlayerInfo() != null ? p.getPlayerInfo().getName() : "null";
            System.out.println("      - " + name + " (ID: " + id + ", Token: " + token + ")");
        }
    }


}
