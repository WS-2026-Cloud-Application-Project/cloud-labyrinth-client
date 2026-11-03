package com.uni.labyrinth.ui;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.model.GameServer;
import com.uni.labyrinth.service.GameServerService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Optional;

public class ServerEntry extends VBox {

    private final Main mainApp;
    private final GameServer server;
    private final GameServerService gameServerService;

    public ServerEntry(Main mainApp, GameServer server, GameServerService gameServerService) {
        this.mainApp = mainApp;
        this.server = server;
        this.gameServerService = gameServerService;

        initUI();
        registerEvents();
    }

    private void initUI() {
        this.setId("serverEntry_" + server.getName().replace(" ", "_"));

        setSpacing(10);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        // Texte
        Label nameLabel = new Label(server.getName());
        nameLabel.setId("serverNameLabel_" + server.getName().replace(" ", "_"));
        nameLabel.setFont(Font.font("Super Mario 256", 20));
        nameLabel.setStyle("-fx-text-fill: black;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(230);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);

        String statusText = switch (server.getStatus()) {
            case LOBBY -> "WARTEN AUF SPIELER";
            case IN_GAME -> "SPIEL LÄUFT";
            case FINISHED -> "SPIEL BEENDET";
        };

        Label statusLabel = new Label(statusText);
        statusLabel.setId("statusLabel_" + server.getName().replace(" ", "_"));
        statusLabel.setFont(Font.font("Press Start 2P", 12));
        statusLabel.setStyle("-fx-text-fill: black;");

        Label statusPlayer = new Label(String.format("%d/%d Spieler belegt", server.getCurrentPlayerCount(), server.getMaxPlayers()));
        statusPlayer.setId("statusPlayerLabel_" + server.getName().replace(" ", "_"));
        statusPlayer.setFont(Font.font("Press Start 2P", 12));
        statusPlayer.setStyle("-fx-text-fill: black;");

        VBox textBox = new VBox(10, nameLabel, statusLabel, statusPlayer);
        textBox.setAlignment(Pos.CENTER);

        // Card die automatisch wachsen darf
        StackPane card = new StackPane(textBox);
        card.setMinWidth(250);
        card.setMaxWidth(250);

        card.setStyle("""
                    -fx-background-color: #29abe2;
                    -fx-background-radius: 30;
                    -fx-border-color: #333333;
                    -fx-border-width: 2px;
                    -fx-border-radius: 30;
                    -fx-padding: 15;
                """);

        getChildren().add(card);
    }

    private void registerEvents() {
        setOnMouseEntered(e -> setCursor(Cursor.HAND));
        setOnMouseExited(e -> setCursor(Cursor.DEFAULT));
        setOnMouseClicked(e -> {

            if ("FINISHED".equals(server.getStatus().toString())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Hinweis");
                alert.setHeaderText(null);
                alert.setContentText("Beitritt nicht möglich - Spiel ist bereits beendet");
                alert.showAndWait();
                return;
            }

            Stage owner = (Stage) getScene().getWindow();
            Optional<String> usernameOpt = UsernameDialog.show(owner);
            if (usernameOpt.isEmpty()) {
                return;
            }

            String userNameStr = usernameOpt.get();
            gameServerService.handleConnectClick(server, mainApp, userNameStr);
        });
    }
}
