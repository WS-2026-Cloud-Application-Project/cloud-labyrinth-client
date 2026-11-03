package com.uni.labyrinth.service.websocket;

import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.domain.enums.TurnState;
import com.uni.labyrinth.model.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public interface GameClientListener {

    void onConnectAck(String playerId, String identifierToken);

    void onServerinfo(OffsetDateTime serverTime, String serverVersion, String protocolVersion, String motd);

    void onLobbyState(List<PlayerInfo> players);

    void onGameStarted(Tile[][] tiles, List<PlayerState> players);

    void onGameStateUpdate(Tile[][] tiles, Tile extraTile, TurnState currentTurnState, String currentPlayerId, List<PlayerState> players, LastPush lastPush, String turnEndTime);

    void onPlayerTurn(String playerId, Tile extraTile);

    void onAchievementUnlocked(String playerId, AchievementType achievement);

    void onError(String message);

    void onGameOver(String winnerId, List<RankingEntry> ranking);

    void onNextTreasure(int id, String name);

}
