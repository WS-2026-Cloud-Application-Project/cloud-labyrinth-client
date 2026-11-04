package com.uni.labyrinth.service.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.domain.enums.TurnState;
import com.uni.labyrinth.domain.events.*;
import com.uni.labyrinth.model.*;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.Session;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ClientEndpoint
public class GameWebSocketClient {

    private final GameClientListener listener;
    private final Gson gson = new Gson();
    private static final Logger log = LoggerFactory.getLogger(GameWebSocketClient.class);
    private static final Logger logGame = LoggerFactory.getLogger("GAME_LOG");
    private Duration offsetServerTime = Duration.ofSeconds(-1L);


    public GameWebSocketClient(GameClientListener listener) {
        this.listener = listener;
    }
    @OnOpen
    public void onOpen(Session session) {

        System.out.println("WebSocket verbunden!");
        log.info(" : onOpen() : WebSocket verbunden!");
    }

    @OnMessage
    public void onMessage(String msg) {
        log.info(": onMessage() : " + msg);
        //System.out.println("WS-Nachricht: " + msg);
        JsonObject json = gson.fromJson(msg, JsonObject.class);

        String type = json.get("type").getAsString();

        switch (type) {
            case "CONNECT_ACK" -> handleConnectAck(msg);
            case "LOBBY_STATE" -> handleLobbyState(msg);
            case "GAME_STARTED" -> handleGameStarted(msg);
            case "GAME_STATE_UPDATE" -> handleGameStateUpdate(msg);
            case "PLAYER_TURN" -> handlePlayerTurn(msg);
            case "ACTION_ERROR" -> handleError(msg);
            case "GAME_OVER" -> handleGameOver(msg);
            case "SERVER_INFO"-> handleServerInfo(msg);
            case "PLAYER_UPDATED"-> handleNotImplemented(msg);
            case "ACHIEVEMENT_UNLOCKED"-> handleAchievementUnlocked(msg);
            case "NEXT_TREASURE" -> handleNextTreasure(msg);
                // Noch nicht implementiert
            }
    }

    private void handleNotImplemented(String msg) {
        log.info(": handleNotImplemented(String msg) : " + msg);

        //System.out.println( msg ); // Placeholder for not implemented handlers
    }



    private void handleServerInfo(String msg) {
        ServerInfoPayload payload = gson.fromJson(msg, ServerInfoPayload.class);
        if( payload == null ) {
            log.error("❌ CRITICAL: ServerInfoPayload ist NULL!");
            return;
        }
        if( payload.getServerTime() == null ) {
            log.error("❌ CRITICAL: serverTime ist NULL im ServerInfoPayload!");
            return;
        }
        OffsetDateTime dateTimeServer = payload.getServerTimeAsOffsetDateTime();
        OffsetDateTime clientNow = OffsetDateTime.now();
        this.offsetServerTime=Duration.between(clientNow,dateTimeServer);
        OffsetDateTime serverNow = dateTimeServer.plus(offsetServerTime);
        listener.onServerinfo(serverNow, payload.getServerVersion(), payload.getProtocolVersion(), payload.getMotd());
        log.info(": handleNotImplemented(String msg) : " + msg);

    }



    private void handleConnectAck(String msg) {
        ConnectAckPayload payload = gson.fromJson(msg, ConnectAckPayload.class);
        listener.onConnectAck(payload.getPlayerId(), payload.getIdentifierToken());
        log.info(": handleConnectAck(String msg) : " + msg);
        logGame.info("Player connected with ID: " + payload.getPlayerId());
        logGame.info("Player connected with identifierToken: " + payload.getIdentifierToken());
    }

    private void handleLobbyState(String msg) {
        LobbyStateEventPayload payload = gson.fromJson(msg, LobbyStateEventPayload.class);

        List<PlayerInfo> players = payload.getPlayers().stream()
                .map(p -> new PlayerInfo(
                        p.getId(),
                        p.getName(),
                        p.getColor(),
                        p.getIsAdmin(),
                        p.getIsAiControlled(),
                        p.getIsConnected()
                ))
                .toList();
        listener.onLobbyState(players);
        log.info(": handleLobbyState(String msg) : " + msg);
        logGame.info("Player lobby state: " + msg);
    }

    private void handleError(String msg) {
        ActionErrorEventPayload payload = gson.fromJson(msg, ActionErrorEventPayload.class);
        listener.onError(payload.getMessage());
        log.info(": handleError(String msg) : " + msg);
        logGame.info("Action error: " + payload.getMessage());
    }

    private void handleGameStarted(String msg) {
        GameStartedPayload payload = gson.fromJson(msg, GameStartedPayload.class);

        // parse payload to 2D tile array
        Tile[][] tiles = buildTiles(payload.getBoard());
        List<PlayerState> players = payload.getPlayers();
        listener.onGameStarted(tiles, players);
        log.info(": handleGameStarted(String msg) : " + msg);
        logGame.info("Player game started: " + msg);
        handleGameStateUpdate( msg);
    }

    private void handleGameStateUpdate(String msg) {
        GameStateUpdatePayload payload = gson.fromJson(msg, GameStateUpdatePayload.class);
        Tile[][] tiles = buildTiles(payload.getBoard());
        Tile extraTile = payload.getExtraTile();
        TurnInfo currentTurnInfo = payload.getCurrentTurnInfo();

        // Check if currentTurnInfo is null first
        if (currentTurnInfo == null) {
            log.error("❌ CRITICAL: currentTurnInfo is NULL! Cannot process game state update.");
            return;
        }

        TurnState currentTurnState = currentTurnInfo.getState();
        if( currentTurnState == null ) {
            log.warn("⚠️ turnState ist NULL! wird auf WAITING_FOR_PUSH gesetzt");
            currentTurnState=TurnState.WAITING_FOR_PUSH;
            currentTurnInfo.setState(TurnState.WAITING_FOR_PUSH);
        }
        if( currentTurnInfo.getTurnEndTime() == null ) {
            log.warn("⚠️ Laufzeit = null  auf 60 Sekunden gestellt");

            LocalDateTime result = LocalDateTime.now().plusSeconds(60);
            currentTurnInfo.setTurnEndTime(result.toString());
        }else {
            OffsetDateTime serverTurnEndTime = OffsetDateTime.parse(currentTurnInfo.getTurnEndTime());
            OffsetDateTime adjustedTurnEndTime = serverTurnEndTime.plusNanos(offsetServerTime.toNanos());
            currentTurnInfo.setTurnEndTime(adjustedTurnEndTime.toString());
        }
        // WICHTIG: currentPlayerId ist in currentTurnInfo, nicht auf Root-Ebene!
        String currentPlayerId = currentTurnInfo.getCurrentPlayerId();
        List<PlayerState> players = payload.getPlayers();
        LastPush lastPush = payload.getBoard().getLastPush();

        // DEBUG: Zeige Server-Daten
        System.out.println("🔍 DEBUG handleGameStateUpdate:");
        System.out.println("   currentTurnInfo: " + (currentTurnInfo != null ? "vorhanden" : "NULL!"));
        System.out.println("   currentTurnState: " + currentTurnState);
        System.out.println("   currentPlayerId: " + currentPlayerId);
        System.out.println("   currentTurntime: " + currentTurnInfo.getTurnEndTime());
        System.out.println("   lastPush: " + (lastPush != null ? "Index=" + lastPush.getRowOrColumnIndex() + ", Dir=" + lastPush.getDirection() : "null"));

        log.info(": handleGameStateUpdate(String msg) : " + msg);
        logGame.info("Player game state: " + msg);

        // Übergebe auch turnEndTime für AFK-Feature
        String turnEndTime = currentTurnInfo.getTurnEndTime();
        listener.onGameStateUpdate(tiles, extraTile, currentTurnState, currentPlayerId, players, lastPush, turnEndTime);
    }

    private void handlePlayerTurn(String msg) {
        PlayerTurnPayload payload = gson.fromJson(msg, PlayerTurnPayload.class);
        String playerId = payload.getPlayerId();
        Tile extraTile = payload.getExtraTile();
        listener.onPlayerTurn(playerId, extraTile);
        logGame.info("Player turn: " + msg);
    }

    private void handleAchievementUnlocked(String msg) {
        AchievementUnlockedPayload payload = gson.fromJson(msg, AchievementUnlockedPayload.class);
        String playerId = payload.getPlayerId();
        AchievementType achievement = payload.getAchievement();
        listener.onAchievementUnlocked(playerId, achievement);
        logGame.info("Player achievement unlocked: " + msg);
    }

    private void handleGameOver(String msg) {
        GameOverPayload payload = gson.fromJson(msg, GameOverPayload.class);
        String winnerId = payload.getWinnerId();
        List<RankingEntry> ranking = payload.getRanking();
        listener.onGameOver(winnerId, ranking);
        log.info(": handleGameOver(String msg) : " + msg);
        logGame.info("Player game over: " + winnerId);
    }

    private void handleNextTreasure(String msg) {
        NextTreasureEventPayload payload = gson.fromJson(msg, NextTreasureEventPayload.class);
        int id = payload.getTreasure().getId();
        String name = payload.getTreasure().getName();
        listener.onNextTreasure(id, name);
        log.info(": handleNextTreasure(String msg) : " + msg);
        logGame.info("Player next treasure: " + id);
    }


    private Tile[][] buildTiles(GameBoard boardData) {
        int rows = boardData.getRows();
        int cols = boardData.getCols();
        Tile[][] tiles = new Tile[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Tile t = boardData.getTiles().get(y).get(x);
                List<Direction> entrances= t.getEntrances();
                tiles[y][x] = new Tile(
                    entrances,
                    t.isFixed(),
                    t.getTreasure(),
                    t.getBonus()
                );
            }
        }
        return tiles;
    }


    @OnClose
    public void onClose(Session session, javax.websocket.CloseReason reason) {
        System.out.println("WS geschlossen: " + reason);
        log.info(": onClose() :WS geschlossen: " + reason);
        logGame.info("Verbindung zu Server geschlossen!");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}
