package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.model.GameServer;
import com.uni.labyrinth.service.GameServerService;
import com.uni.labyrinth.ui.BackgroundLayout;
import com.uni.labyrinth.ui.ServerEntry;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerListScreen {

    private final Pane root;
    private final Timeline refreshTimeline;

    public ServerListScreen(Main mainApp, GameServerService gameServerService) throws IOException, InterruptedException {
        VBox ui = new VBox(20);
        BackgroundLayout bg = new BackgroundLayout();

        Label textField = new Label("Serverauswahl");
        textField.setFont(Font.font("Super Mario 256", 45));
        textField.setAlignment(Pos.CENTER);

        TilePane serverListContainer = new TilePane();
        serverListContainer.setId("serverListContainer");
        serverListContainer.setHgap(10);
        serverListContainer.setVgap(10);
        serverListContainer.setPrefColumns(4);
        serverListContainer.setTileAlignment(Pos.TOP_CENTER);
        serverListContainer.setAlignment(Pos.TOP_CENTER);
        serverListContainer.setStyle("-fx-background-color: transparent;");

        ScrollPane scrollPane = new ScrollPane(serverListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        //  ScrollPane Hintergrund transparent
        scrollPane.setStyle("""
                -fx-background-color: transparent;
                -fx-background: transparent;
                """);
        // CSS für schwarze Scrollbar laden
        scrollPane.getStylesheets().add(getClass().getResource("/scrollbar.css").toExternalForm());

        // Serverliste regelmäßig aktualisieren
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            try {
                refreshServers(serverListContainer, gameServerService, mainApp);
                System.out.println("Serverliste aktualisiert");
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();

        //Initial laden
        refreshServers(serverListContainer, gameServerService, mainApp);

        ui.getChildren().addAll(textField, scrollPane);
        ui.setAlignment(Pos.TOP_CENTER);
        ui.setPadding(new Insets(25, 50, 70, 50));

        root = new StackPane(bg.getBackground(), ui);
    }

    public Pane getRoot() {
        return root;
    }

    public void stop() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    private void refreshServers(TilePane serverListContainer, GameServerService gameServerService, Main mainApp) throws IOException, InterruptedException {
        serverListContainer.getChildren().clear();

        List<GameServer> servers = new ArrayList<>(gameServerService.getServerList());

        for (GameServer server : servers) {

            String uri = server.getUri();
            boolean endetMitGame = uri.endsWith("/game");
            if (!(uri.startsWith("wss://")) && (!(uri.startsWith("ws://")))) {
                uri = "ws://" + uri;
                server.setUri(uri);
            }
            if (!endetMitGame) {
                if (!uri.endsWith("/")) {
                    uri += "/";
                }
                uri += "game";
                server.setUri(uri);
            }
            serverListContainer.getChildren().add(new ServerEntry(mainApp, server, gameServerService));
        }
    }
}
