package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.model.LobbyState;
import com.uni.labyrinth.model.PlayerInfo;
import com.uni.labyrinth.service.GameServerService;
import com.uni.labyrinth.ui.BackgroundLayout;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LobbyWaitingScreen {

    private final Pane root;
    private final LobbyState lobbyState;
    private final GameServerService gameServerService;

    private Text baseText;

    public LobbyWaitingScreen(Main mainApp) {
        this.lobbyState = mainApp.getLobbyState();
        this.gameServerService = mainApp.getGameServerService();

        BackgroundLayout bg = new BackgroundLayout();
        Label headline = new Label("Warteraum");
        headline.setFont(Font.font("Super Mario 256", 24));

        HBox waitingText = createWaitingText();

        Button startGameButton = new Button("Spiel starten!");
        startGameButton.setId("startGameButton");
        startGameButton.setStyle("-fx-background-color: #4CAF50;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        startGameButton.setOnMouseEntered(e -> startGameButton.setCursor(Cursor.HAND));
        startGameButton.setOnMouseExited(e -> startGameButton.setCursor(Cursor.DEFAULT));


        startGameButton.setVisible(false);
        startGameButton.setManaged(false);

        startGameButton.setOnAction(e -> {
            Stage owner = (Stage) startGameButton.getScene().getWindow();
            gameServerService.sendStartGameCommand(lobbyState.getCurrentPlayers(), owner);
        });

        Button leaveLobbyButton = new Button("Zurück zur Serverauswahl!");
        leaveLobbyButton.setId("leaveLobbyButton");
        leaveLobbyButton.setStyle("-fx-background-color:  #971919;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        leaveLobbyButton.setOnMouseEntered(e -> leaveLobbyButton.setCursor(Cursor.HAND));
        leaveLobbyButton.setOnMouseExited(e -> leaveLobbyButton.setCursor(Cursor.DEFAULT));


        leaveLobbyButton.setVisible(true);
        leaveLobbyButton.setManaged(true);
        leaveLobbyButton.setOnAction(e -> {
            gameServerService.sendDisconnectCommand(mainApp);
        });

        //Zeige daten ohne LobbyStateUpdate
        PlayerInfo me2 = lobbyState.getMyPlayer();
        if (me2 != null) {
            boolean isAdmin = me2.getIsAdmin();
            startGameButton.setVisible(isAdmin);
            startGameButton.setManaged(isAdmin);
        }

        updateWaitingText();

        // Listener: wird aufgerufen, wenn sich lobbyState ändert
        lobbyState.setOnUpdate(() -> Platform.runLater(() -> {
            PlayerInfo me = lobbyState.getMyPlayer();
            if (me != null) {
                boolean isAdmin = me.getIsAdmin();
                startGameButton.setVisible(isAdmin);
                startGameButton.setManaged(isAdmin);
            }
            updateWaitingText();
        }));

        HBox buttonsBox = new HBox(10, startGameButton, leaveLobbyButton);
        buttonsBox.setAlignment(Pos.CENTER);
        VBox ui = new VBox(25, headline, waitingText, buttonsBox);

        ui.setAlignment(Pos.CENTER);

        root = new StackPane(bg.getBackground(), ui);
    }

    private HBox createWaitingText() {
        baseText = new Text(lobbyState.getCurrentPlayers() + "/" + lobbyState.getMaxPlayers() + " Warten auf weitere Spieler");
        baseText.setId("waitingText");
        baseText.setFont(Font.font("Press Start 2P", 12));

        Text dotsText = new Text("");
        dotsText.setFont(Font.font("Press Start 2P", 12));

        int maxDots = 3;
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            int count = dotsText.getText().length();
            count = (count + 1) % (maxDots + 1);

            dotsText.setText(".".repeat(count));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        TextFlow textFlow = new TextFlow(baseText, dotsText);
        textFlow.setTextAlignment(TextAlignment.LEFT);

        StackPane dotsPane = new StackPane(textFlow);
        dotsPane.setPrefWidth(50);

        HBox waitingText = new HBox(baseText, dotsPane);
        waitingText.setAlignment(Pos.CENTER);

        return waitingText;
    }

    private void updateWaitingText() {
        int currentPlayerCount = lobbyState.getCurrentPlayers();
        int maxPlayerCount = lobbyState.getMaxPlayers();
        if (currentPlayerCount < maxPlayerCount) {
            baseText.setText(currentPlayerCount + "/" + maxPlayerCount + " Warten auf weitere Spieler");
        } else {
            baseText.setText(currentPlayerCount + "/" + maxPlayerCount + " Warten auf Spielstart");
        }
    }

    public Pane getRoot() {
        return root;
    }
}
