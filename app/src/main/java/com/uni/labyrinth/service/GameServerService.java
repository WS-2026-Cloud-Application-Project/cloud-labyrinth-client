package com.uni.labyrinth.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.commands.*;
import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.domain.enums.Status;
import com.uni.labyrinth.model.BoardSize;
import com.uni.labyrinth.model.Coordinates;
import com.uni.labyrinth.model.GameServer;
import com.uni.labyrinth.screens.ServerListScreen;
import com.uni.labyrinth.service.timemeasurement.LapTimer;
import com.uni.labyrinth.service.websocket.GameWebSocketClient;
import com.uni.labyrinth.ui.ErrorDialog;
import com.uni.labyrinth.ui.GameStartDialog;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GameServerService {
    private static final Logger log = LoggerFactory.getLogger(GameServerService.class);
    private static final Logger logGame = LoggerFactory.getLogger("GAME_LOG");
    private static final String TAG = Config.get("mgmt.tag");
    private static final String API_URL =
            Config.get("mgmt.base-url").replaceAll("/$", "") + "/" + TAG.replaceAll("^/", "");
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private Session currentSession;

    public List<GameServer> getServerList() throws IOException, InterruptedException {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Type listType = new TypeToken<List<GameServer>>() {
            }.getType();
            return gson.fromJson(response.body(), listType);
        } catch (Exception e) {
            log.info("Fehler beim Abrufen der Serverliste: " + e.getMessage());
            log.info("Verwende stattdessen die Mock-Serverliste.");
            System.err.println("Fehler beim Abrufen der Serverliste: " + e.getMessage());
            System.err.println("Verwende stattdessen die Mock-Serverliste.");
            return getMockServerList();
        }
    }

    // ===== MOCK-METHODE ===== //
    public List<GameServer> getMockServerList() {
        List<GameServer> servers = new ArrayList<>();

        GameServer server1 = new GameServer();
        server1.setId(UUID.randomUUID());
        server1.setName("MCI Pro Labyrinth");
        server1.setUri("ws://localhost:8397/game");
        server1.setMaxPlayers(4);
        server1.setCurrentPlayerCount(0);
        server1.setStatus(Status.LOBBY);

        GameServer server2 = new GameServer();
        server2.setId(UUID.randomUUID());
        server2.setName("Adventure Arena");
        server2.setUri("ws://localhost:8398/game");
        server2.setMaxPlayers(4);
        server2.setCurrentPlayerCount(0);
        server2.setStatus(Status.LOBBY);

        servers.add(server1);
        servers.add(server2);

        return servers;
    }

    public void handleConnectClick(GameServer server, Main mainApp, String username) {
        mainApp.getGameServerService().connectToServer(mainApp, server, username).exceptionally(ex -> {
            Platform.runLater(() -> {
                Stage stage = (Stage) Stage.getWindows().get(0);
                ErrorDialog.show(stage, "Verbindungsfehler", "Server nicht erreichbar", ex.getMessage());
            });
            log.info("GameserverService:handleConnectClick triggered");
            return null;
        });
    }

    public CompletableFuture<Session> connectToServer(Main mainApp, GameServer server, String username) {
        return connectToServer(mainApp, server, username, mainApp.getLobbyState().getIdentifierToken());
    }

    public CompletableFuture<Session> connectToServer(Main mainApp, GameServer server, String username, String identifierToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                System.out.println("WebSocket verbunden mit Server: " + server.getUri());
                Session session = container.connectToServer(new GameWebSocketClient(mainApp), URI.create(server.getUri()));

                sendConnectCommand(session, username, identifierToken);
                this.currentSession = session;
                return session;
            } catch (Exception e) {
                log.error("connectToServer:WebSocket-Verbindung fehlgeschlagen: " + e.getMessage());
                throw new RuntimeException("WebSocket-Verbindung fehlgeschlagen: " + e.getMessage(), e);
            }
        });
    }

    private void sendConnectCommand(Session session, String username, String identifierToken) {
        if (session != null && session.isOpen()) {
            ConnectCommandPayload payload = new ConnectCommandPayload("CONNECT", username, identifierToken);
            String json = gson.toJson(payload);
            session.getAsyncRemote().sendText(json);
            logGame.info("sendConnectCommand: " + json);
        }
    }

    public void sendMovePlayer(Coordinates coordinates) {
        if (currentSession != null && currentSession.isOpen()) {
            MovePawnCommandPayload payload = new MovePawnCommandPayload();
            payload.setTargetCoordinates(coordinates);
            payload.setType(CommandType.MOVE_PAWN);

            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendMovePlayer: " + json);
        }
    }

    public void sendSwapCommand(String playerId) {
        if (currentSession != null && currentSession.isOpen()) {
            UseSwapCommandPayload payload = new UseSwapCommandPayload(playerId);
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendSwapCommand: " + json);
        }
    }

    public void sendPushTwiceCommand() {
        if (currentSession != null && currentSession.isOpen()) {
            UsePushTwiceCommandPayload payload = new UsePushTwiceCommandPayload();
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendPushTwiceCommand: " + json);
        }
    }

    public void sendBeamCommand(Coordinates coordinates) {
        if (currentSession != null && currentSession.isOpen()) {
            UseBeamCommandPayload payload = new UseBeamCommandPayload(coordinates);
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendBeamCommand: " + json);
        }
    }

    public void sendPushFixed(int rowOrColIndex, Direction direction) {
        if (currentSession != null && currentSession.isOpen()) {
            UsePushFixedCommandPayload payload = new UsePushFixedCommandPayload(rowOrColIndex, direction);
            String json = gson.toJson(payload);
            System.out.println(json);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendPushFixed: " + json);
        }
    }

    public void sendPushTile(int rowOrColIndex, Direction direction) {
        if (currentSession != null && currentSession.isOpen()) {
            PushTileCommandPayload payload = new PushTileCommandPayload(rowOrColIndex, direction);
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendPushTile: " + json);
        }
    }

    public void sendRotateCommand() {
        if (currentSession != null && currentSession.isOpen()) {
            LapTimer.reset();
            RotateTileCommandPayload payload = new RotateTileCommandPayload();
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendRotateCommand: " + json);
        }
    }

    public void sendStartGameCommand(int playerCount, Stage primaryStage) {
        GameStartDialog popup = new GameStartDialog();
        popup.show(primaryStage, config -> {

            if (currentSession != null && currentSession.isOpen()) {
                StartGameCommandPayload payload = new StartGameCommandPayload(CommandType.START_GAME, new BoardSize(config.boardSize(), config.boardSize()), (config.treasureCountPerPlayer()) * playerCount, config.gameDurationInSeconds(), config.bonusCount());

                String json = gson.toJson(payload);
                currentSession.getAsyncRemote().sendText(json);
                logGame.info("sendStartGameCommand: " + json);
            }
        });
    }

    public void sendDisconnectCommand(Main mainApp) {
        if (currentSession != null && currentSession.isOpen()) {
            DisconnectCommandPayload payload = new DisconnectCommandPayload();
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendDisconnectCommand: " + json);
        }
        mainApp.getLobbyState().setIdentifierToken(null);
        mainApp.resetCurrentGameScreenToNull();
        try {
            mainApp.setScreen(new ServerListScreen(mainApp, mainApp.getGameServerService()).getRoot());
            currentSession.close();
            log.info("disconnect:  successful");
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void sendToggleAI(boolean enabled) {
        if (currentSession != null && currentSession.isOpen()) {
            ToggleAiCommandPayload payload = new ToggleAiCommandPayload(enabled);
            String json = gson.toJson(payload);
            currentSession.getAsyncRemote().sendText(json);
            logGame.info("sendToggleAI: " + json + " (enabled=" + enabled + ")");
        }
    }

    public Session getCurrentSession() {
        return currentSession;
    }


    public void shutdownCurrentSession() {
        if (currentSession != null && currentSession.isOpen()) {
            try {
                DisconnectCommandPayload payload = new DisconnectCommandPayload();
                String json = gson.toJson(payload);

                // Synchron senden (wartet bis fertig)
                currentSession.getBasicRemote().sendText(json);
                logGame.info("sendDisconnectCommand (sync): " + json);

                currentSession.close(new CloseReason(
                        CloseReason.CloseCodes.NORMAL_CLOSURE, "Client shutdown"));
                log.info("Current session closed successfully.");
            } catch (Exception e) {
                log.error("Error during shutdownCurrentSession: {}", e.getMessage(), e);
                try { currentSession.close(); } catch (Exception ignored) {}
            }
        } else {
            log.info("No open session to close.");
        }
    }



}
