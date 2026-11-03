package com.uni.labyrinth;

import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.domain.enums.TurnState;
import com.uni.labyrinth.domain.events.ServerInfoPayload;
import com.uni.labyrinth.model.*;
import com.uni.labyrinth.screens.GameScreen;
import com.uni.labyrinth.screens.LobbyWaitingScreen;
import com.uni.labyrinth.screens.ResultScreen;
import com.uni.labyrinth.screens.ServerListScreen;
import com.uni.labyrinth.service.GameServerService;
import com.uni.labyrinth.service.websocket.GameClientListener;
import com.uni.labyrinth.ui.ErrorDialog;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main extends javafx.application.Application implements GameClientListener {

    private StackPane root;
    private GameServerService gameServerService;
    private LobbyState lobbyState;
    private GameState gameState;
    private GameScreen currentGameScreen; // Referenz zum aktuellen GameScreen für AI-Retry
    private ServerListScreen serverListScreen;
    private ServerInfo serverinfo;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        Font.loadFont(getClass().getResourceAsStream("/fonts/PressStart2P-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/SuperMario256.ttf"), 14);

        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/appiconimage.png")));
        stage.setTitle("Labyrinth - Super Mario Edition");

        root = new StackPane();
        Scene scene = new Scene(root, 1200, 900);

        stage.setOnCloseRequest(event -> {
            event.consume(); // verhindert sofortiges Schließen

            // optional: UI "busy" / Dialog / etc. (wenn du willst)
            gracefulShutdownAndExit(stage);
        });


        stage.setScene(scene);
        stage.show();

        gameServerService = new GameServerService();

        lobbyState = new LobbyState();
        gameState = new GameState();
        serverinfo= new ServerInfo();

        serverListScreen = (new ServerListScreen(this, gameServerService));
        setScreen(serverListScreen.getRoot());
    }

    @Override
    public void onConnectAck(String playerId, String identifierToken) {
        Platform.runLater(() -> {
            lobbyState.setMyPlayerId(playerId);
            lobbyState.setIdentifierToken(identifierToken);
            gameState.setMyPlayerId(playerId);
            serverListScreen.stop();
            if (currentGameScreen != null) {
                setScreen(currentGameScreen.getRoot());
            } else {
                setScreen(new LobbyWaitingScreen(this).getRoot());
            }
        });
    }

    @Override
    public void onServerinfo (OffsetDateTime serverTime, String serverVersion, String protocolVersion, String motd) {
        Platform.runLater(() -> {
            serverinfo.setServerVersion(serverVersion);
            serverinfo.setServerTime(serverTime);
            serverinfo.setProtocolVersion(protocolVersion);
            serverinfo.setMotd(motd);
        });

    }

    @Override
    public void onLobbyState(List<PlayerInfo> players) {
        Platform.runLater(() -> {
            lobbyState.setPlayers(players);

            PlayerInfo me = players.stream().filter(p -> p.getId().equals(lobbyState.getMyPlayerId())).findFirst().orElse(null);

            if (me != null) {
                lobbyState.setMyPlayer(me);
            }
        });
    }

    @Override
    public void onGameStarted(Tile[][] tiles, List<PlayerState> players) {
        Platform.runLater(() -> {
            gameState.setPlayers(players);
            gameState.setBoard(tiles);
            currentGameScreen = new GameScreen(gameState, gameServerService, this);
            setScreen(currentGameScreen.getRoot());
        });
    }

    @Override
    public void onGameStateUpdate(Tile[][] tiles, Tile extraTile, TurnState currentTurnState, String currentPlayerId, List<PlayerState> players, LastPush lastPush, String turnEndTime) {
        Platform.runLater(() -> {
            gameState.setLastPush(lastPush);
            gameState.applyServerUpdate(tiles, extraTile, currentTurnState, gameState.getPlayerById(currentPlayerId), currentPlayerId, players);

            // Übergebe turnEndTime an GameScreen für AFK-Feature
            if (currentGameScreen != null) {
                currentGameScreen.onTurnUpdate(currentPlayerId, turnEndTime);
            }
        });
    }

    @Override
    public void onPlayerTurn(String playerId, Tile extraTile) {
        Platform.runLater(() -> {
            gameState.setCurrentPlayer(gameState.getPlayerById(playerId));
            gameState.setCurrentPlayerId(gameState.getPlayerById(playerId).getPlayerInfo().getId());
            gameState.setExtraTile(extraTile);
        });
    }

    @Override
    public void onError(String message) {
        System.out.println("❌ ERROR: " + message);

        if (currentGameScreen != null) {
            if (isScreenShown(currentGameScreen.getRoot())) {
                // AUTOMATISCHER RETRY bei Push-Back Fehler (wenn AI aktiv ist)
                if (message.contains("push back")) {
                    System.out.println("🔄 Push-Back Fehler erkannt - versuche AI-Retry...");
                    Platform.runLater(() -> {
                        currentGameScreen.handlePushBackError();
                    });
                    return;
                } else {
                    Platform.runLater(() -> {
                        currentGameScreen.handleGeneralError(message);
                    });
                    return;
                }
            }
        }
        Platform.runLater(() -> {
            Stage stage = (Stage) Stage.getWindows().get(0);
            ErrorDialog.show(stage, "Fehler", "Servermeldung", message);
        });
    }

    @Override
    public void onNextTreasure(int id, String name) {
        Platform.runLater(() -> {
            gameState.setCurrentTreasure(id, name);
        });
    }

    @Override
    public void onAchievementUnlocked(String playerId, AchievementType achievement) {
        // Spieler entsprechend der Id aus der payload holen
        PlayerState player = gameState.getPlayerById(playerId);

        // Achievement-Liste des Players holen
        List<AchievementType> achievementList = player.getAvailableAchievements();

        // Liste erstellen falls noch null
        if (achievementList == null) {
            achievementList = new ArrayList<>();
        }

        // achievement aus payload zur Liste hinzufügen
        achievementList.add(achievement);

        // neue Liste am Player setzen
        player.setAvailableAchievements(achievementList);
    };

    @Override
    public void onGameOver(String winnerId, List<RankingEntry> ranking) {
        System.out.println(" SPIEL BEENDET! Gewinner: " + winnerId);

        // KRITISCH: Deaktiviere Auto-KI SOFORT, nicht erst in Platform.runLater()
        // Verhindert Race Condition: GAME_STATE_UPDATE könnte vor disableAutoAI() ankommen
        if (currentGameScreen != null) {
            currentGameScreen.disableAutoAI();
        }

        Platform.runLater(() -> {
            setScreen(new ResultScreen(this, winnerId, ranking, gameState).getRoot());
        });
    }
    private volatile boolean shuttingDown = false;

    private void gracefulShutdownAndExit(Stage stage) {
        if (shuttingDown) return;
        shuttingDown = true;

        // Wenn du willst: erst alle UI-Aktionen deaktivieren
        // stage.getScene().getRoot().setDisable(true);

        // Cleanup NICHT im FX-Thread blockieren, sonst hängt das Fenster beim Schließen
        Thread shutdownThread = new Thread(() -> {
            try {
                // 1) Screens stoppen (z.B. ServerListScreen hat stop())
                if (serverListScreen != null) {
                    try { serverListScreen.stop(); } catch (Exception ignored) {}
                }

                // 2) GameScreen / Auto-AI stoppen
                if (currentGameScreen != null) {
                    try { currentGameScreen.disableAutoAI(); } catch (Exception ignored) {}
                    // falls GameScreen eigene Timer/Threads hat: hier stoppen
                }

                // 3) Netzwerk sauber schließen
                if (gameServerService != null) {
                    try {
                        // Beispiel: je nachdem wie dein Service heißt:
                        // gameServerService.disconnect();
                        // gameServerService.close();
                        // gameServerService.shutdown();
                        gameServerService.shutdownCurrentSession(); // <- passe das an deine echte Methode an
                    } catch (Exception ignored) {}
                }

            } finally {
                // 4) Danach sauber beenden (im FX-Thread)
                Platform.runLater(() -> {
                    try {
                        stage.close();
                    } finally {
                        Platform.exit();

                        // Fallback, falls Non-Daemon-Threads das JVM-Ende verhindern:
                        System.exit(0);
                    }
                });
            }
        });

        shutdownThread.setName("graceful-shutdown");
        shutdownThread.setDaemon(true); // wichtig: Shutdown-Thread soll Exit nicht blockieren
        shutdownThread.start();
    }

    @Override
    public void stop() {
        // Wird bei Platform.exit() aufgerufen
        // Hier KEIN UI-Kram mehr machen, nur Cleanup
        try {
            if (gameServerService != null) {
                try { gameServerService.shutdownCurrentSession(); } catch (Exception ignored) {}
            }
        } finally {
            // kein System.exit hier nötig (nur wenn du Probleme hast)
        }
    }

    public boolean isScreenShown(Pane screenRoot) {
        return root.getChildren().contains(screenRoot);
    }

    public GameServerService getGameServerService() {
        return gameServerService;
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void resetCurrentGameScreenToNull() {
        this.currentGameScreen = null;
    }

    public void setScreen(Pane screenRoot) {
        root.getChildren().setAll(screenRoot);

        // Background-Layout an Fenstergröße dynamisch anpassen
        screenRoot.prefWidthProperty().bind(root.widthProperty());
        screenRoot.prefHeightProperty().bind(root.heightProperty());
    }
}
