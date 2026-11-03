package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.model.GameState;
import com.uni.labyrinth.model.PlayerState;
import com.uni.labyrinth.model.RankingEntry;
import com.uni.labyrinth.ui.BackgroundLayout;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;

import static javafx.scene.text.TextAlignment.CENTER;

public class ResultScreen {

    private final Pane root;

    public ResultScreen(Main mainApp, String winnerId, List<RankingEntry> ranking, GameState gameState) {
        BackgroundLayout bg = new BackgroundLayout();

        // Titel
        Text titleLabel = new Text("SPIEL BEENDET");
        titleLabel.setFont(Font.font("Super Mario 256", 45));
        titleLabel.setFill(Color.GOLD);
        titleLabel.setStroke(Color.WHITE);
        titleLabel.setStrokeWidth(2);

        // Gewinner-Anzeige
        PlayerState winner = gameState.getPlayerById(winnerId);
        String winnerName = winner != null ? winner.getPlayerInfo().getName() : "Unbekannt";
        String winnerColor = winner != null ? winner.getPlayerInfo().getColor() : "#FFFFFF";

        HBox winnerBox = new HBox(3);
        winnerBox.setAlignment(Pos.BASELINE_CENTER);
        Text baseLabel = new Text("Gewinner: ");
        baseLabel.setFont(Font.font("Super Mario 256", 25));
        baseLabel.setFill(Color.WHITE);
        Text winnerLabel = new Text(winnerName);
        winnerLabel.setId("winnerLabel");
        winnerLabel.setFont(Font.font("Press Start 2P", 15));
        winnerLabel.setFill(Color.web(winnerColor));

        winnerBox.getChildren().addAll(baseLabel, winnerLabel);

        // Rangliste
        VBox rankingBox = new VBox(15);
        rankingBox.setAlignment(Pos.CENTER);
        rankingBox.setPadding(new Insets(20));
        rankingBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");
        rankingBox.setMinWidth(600);
        rankingBox.setMaxWidth(750);

        Label rankingTitle = new Label("RANGLISTE");
        rankingTitle.setFont(Font.font("Super Mario 256", 24));
        rankingTitle.setTextFill(Color.WHITE);

        rankingBox.getChildren().add(rankingTitle);

        // Füge jeden Spieler zur Rangliste hinzu
        for (RankingEntry entry : ranking) {
            PlayerState player = gameState.getPlayerById(entry.getPlayerId());
            if (player != null) {
                HBox playerRow = createRankingRow(entry, player);
                rankingBox.getChildren().add(playerRow);
            }
        }

        // Zurück-Button
        Button backBtn = new Button("Lobby verlassen");
        backBtn.setId("backBtn");
        backBtn.setStyle("-fx-background-color:  #971919;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 24px;");
        backBtn.setOnMouseEntered(e -> backBtn.setCursor(Cursor.HAND));
        backBtn.setOnMouseExited(e -> backBtn.setCursor(Cursor.DEFAULT));
        backBtn.setOnAction(e -> {
            mainApp.getGameServerService().sendDisconnectCommand(mainApp);
        });

        // GleicheLobby-Button
        Button lobbyBtn = new Button("Nochmal spielen");
        lobbyBtn.setId("lobbyBtn");
        lobbyBtn.setStyle("-fx-background-color:  #4CAF50;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 24px;");
        lobbyBtn.setOnMouseEntered(e -> lobbyBtn.setCursor(Cursor.HAND));
        lobbyBtn.setOnMouseExited(e -> lobbyBtn.setCursor(Cursor.DEFAULT));
        lobbyBtn.setOnAction(e -> {
            mainApp.resetCurrentGameScreenToNull();
            mainApp.getLobbyState();
            mainApp.setScreen(new LobbyWaitingScreen(mainApp).getRoot());
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(lobbyBtn, backBtn);

        VBox ui = new VBox(20, titleLabel, winnerBox, rankingBox, buttonBox);
        ui.setAlignment(Pos.CENTER);
        ui.setPadding(new Insets(30));

        root = new StackPane(bg.getBackground(), ui);
    }

    private HBox createRankingRow(RankingEntry entry, PlayerState player) {
        HBox row = new HBox(20);
        row.setId("rankingRow_" + player.getPlayerInfo().getId());
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 10, 10, 30));
        row.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5;");
        row.setMinWidth(700);
        row.setMaxWidth(700);

        // Rang
        Label rankLabel = new Label(entry.getRank() + ".");
        rankLabel.setId("rankLabel_" + player.getPlayerInfo().getId());
        int rank = 1;
        if (entry.getRank() > 0 && entry.getRank() < 5) {
            rank = 24 - (entry.getRank() - 1) * 4;
        }

        rankLabel.setFont(Font.font("Super Mario 256", rank));
        rankLabel.setTextFill(getRankColor(entry.getRank()));
        rankLabel.setMinWidth(30);

        // Spieler-Farbe Circle
        Circle colorCircle = new Circle(8);
        colorCircle.setId("colorCircle_" + player.getPlayerInfo().getId());
        colorCircle.setFill(Color.web(player.getPlayerInfo().getColor()));
        colorCircle.setStroke(Color.WHITE);
        colorCircle.setStrokeWidth(2);

        // Spieler-Name
        Label nameLabel = new Label(player.getPlayerInfo().getName());
        nameLabel.setId("nameLabel_" + player.getPlayerInfo().getId());
        nameLabel.setFont(Font.font("Press Start 2P", 14));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setMinWidth(170);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(170);



        // Stats
        VBox statsBox = new VBox(7);
        statsBox.setAlignment(Pos.TOP_CENTER);

        // Get stats from ranking (server-side now handles reconnected players correctly)
        int treasures = entry.getStats() != null ? entry.getStats().getTreasuresCollected() : 0;
        int steps = entry.getStats() != null ? entry.getStats().getStepsTaken() : 0;
        int score = entry.getScore();

        Label scoreLabel = new Label("Punkte: " + score);
        scoreLabel.setId("scoreLabel_" + player.getPlayerInfo().getId());
        scoreLabel.setFont(Font.font("Super Mario 256", 18));
        scoreLabel.setTextFill(Color.GOLD);
        String Schatz = " Schätze";
        if (treasures == 1) {
            Schatz = " Schatz";
        }
        Label treasuresLabel = new Label("🏆 " + treasures + Schatz);
        treasuresLabel.setId("treasuresLabel_" + player.getPlayerInfo().getId());
        treasuresLabel.setFont(Font.font("Press Start 2P", 12));
        treasuresLabel.setTextFill(Color.LIGHTGRAY);

        Label stepsLabel = new Label("👣 " + steps + " Schritte");
        stepsLabel.setId("stepsLabel_" + player.getPlayerInfo().getId());
        stepsLabel.setFont(Font.font("Press Start 2P", 12));
        stepsLabel.setTextFill(Color.LIGHTGRAY);
        statsBox.getChildren().addAll(scoreLabel, treasuresLabel, stepsLabel);
        statsBox.setPrefWidth(200);

        // Achievements
        VBox achievementBox = new VBox(7);
        achievementBox.setAlignment(Pos.TOP_CENTER);

        Label achievementLabel = new Label("Erfolge ");
        achievementLabel.setFont(Font.font("Super Mario 256", 18));
        achievementLabel.setTextFill(Color.GOLD);
        achievementBox.getChildren().add(achievementLabel);

        Label achievementTypeLabel = new Label("");

        // Get stats from player (server-side)
        if (player.getAvailableAchievements() != null) {
            List<AchievementType> achievements = player.getAvailableAchievements();
            if (!(achievements.isEmpty())) {
                for (AchievementType a : achievements) {
                    achievementTypeLabel = new Label(a.toString());
                }
            }
        } else {
            achievementTypeLabel.setText("Mamma mia...\ndiesmal nicht!");
        }

        achievementTypeLabel.setId("achievementTypeLabel_" + player.getPlayerInfo().getId());
        achievementTypeLabel.setFont(Font.font("Press Start 2P", 12));
        achievementTypeLabel.setAlignment(Pos.CENTER);
        achievementTypeLabel.setTextAlignment(TextAlignment.CENTER);
        achievementTypeLabel.setWrapText(true);
        achievementTypeLabel.setPrefWidth(200);
        achievementTypeLabel.setTextFill(Color.LIGHTGRAY);
        achievementBox.getChildren().add(achievementTypeLabel);

        row.getChildren().addAll(rankLabel, colorCircle, nameLabel, statsBox, achievementBox);

        return row;
    }

    private Color getRankColor(int rank) {
        return switch (rank) {
            case 1 -> Color.GOLD;
            case 2 -> Color.SILVER;
            case 3 -> Color.web("#CD7F32"); // Bronze
            default -> Color.LIGHTGRAY;
        };
    }

    public Pane getRoot() {
        return root;
    }
}
