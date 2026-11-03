package com.uni.labyrinth.ui;

import com.uni.labyrinth.model.GameStartConfig;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class GameStartDialog {

    private static void showError(Label errorLabel, String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    public void show(Stage owner, Consumer<GameStartConfig> onConfirm) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT); // wichtig für abgerundete Ecken

        // ===== Topbar =====
        Label title = new Label("Spiel konfigurieren");
        title.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 18px;
                """);

        HBox topBar = new HBox(title, new Region(), closeBtn);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8));
        topBar.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 20 20 0 0;");

        closeBtn.setOnAction(e -> stage.close());

        // ===== Inhalt =====
        Label sizeLabel = new Label("Spielfeldgrösse:");
        sizeLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        TextField sizeField = new TextField();
        sizeField.setPrefWidth(120);
        sizeField.setAlignment(Pos.CENTER);
        GridPane.setFillWidth(sizeField, false);
        sizeField.setId("sizeField");
        sizeField.setPromptText("3 - 11");
        sizeField.setStyle("-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        Label treasureLabel = new Label("Schatzkarten pro Spieler:");
        treasureLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        TextField treasureField = new TextField();
        treasureField.setPrefWidth(120);
        treasureField.setAlignment(Pos.CENTER);
        GridPane.setFillWidth(treasureField, false);
        treasureField.setId("treasureField");
        treasureField.setPromptText("z.B. 3");
        treasureField.setStyle("-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        Label timeLabel = new Label("Spieldauer (min):");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        TextField timeField = new TextField();
        timeField.setPrefWidth(120);
        timeField.setAlignment(Pos.CENTER);
        GridPane.setFillWidth(timeField, false);
        timeField.setId("timeField");
        timeField.setPromptText("z.B. 6");
        timeField.setStyle("-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        Label bonusLabel = new Label("Bonusanzahl:");
        bonusLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        TextField bonusField = new TextField();
        bonusField.setPrefWidth(120);
        bonusField.setAlignment(Pos.CENTER);
        GridPane.setFillWidth(bonusField, false);
        bonusField.setId("bonusField");
        bonusField.setPromptText("z.B. 20");
        bonusField.setStyle("-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        Label errorLabel = new Label("");
        errorLabel.setId("errorLabel");
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px;");
        errorLabel.setVisible(false);

        Button start = new Button("Spiel starten!");
        start.setId("startButton");
        start.setStyle("-fx-background-color: #4CAF50;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        start.setOnMouseEntered(e -> start.setCursor(Cursor.HAND));
        start.setOnMouseExited(e -> start.setCursor(Cursor.DEFAULT));


        Button cancel = new Button("Abbrechen");
        cancel.setId("cancelButton");
        cancel.setStyle("-fx-background-color:  #971919;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        cancel.setOnMouseEntered(e -> cancel.setCursor(Cursor.HAND));
        cancel.setOnMouseExited(e -> cancel.setCursor(Cursor.DEFAULT));


        HBox buttons = new HBox(10, start, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(sizeLabel, 0, 0);
        grid.add(sizeField, 1, 0);
        grid.add(treasureLabel, 0, 1);
        grid.add(treasureField, 1, 1);
        grid.add(timeLabel, 0, 2);
        grid.add(timeField, 1, 2);
        grid.add(bonusLabel, 0, 3);
        grid.add(bonusField, 1, 3);

        VBox content = new VBox(12, grid, errorLabel, buttons);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 0 0 20 20;");

        VBox root = new VBox(topBar, content);

        // ===== Runde Ecken (Background + Clip) =====
        root.setStyle("""
                    -fx-background-color: #1e1e1e;
                    -fx-background-radius: 20;
                """);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        // ===== Drop Shadow =====
        DropShadow shadow = new DropShadow();
        shadow.setRadius(25);
        shadow.setSpread(0.2);
        shadow.setOffsetY(8);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        root.setEffect(shadow);

        // ===== Scene transparent =====
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        cancel.setOnAction(e -> stage.close());

        Runnable confirm = () -> {
            try {
                int size = Integer.parseInt(sizeField.getText().trim());
                int treasuresPerPlayer = Integer.parseInt(treasureField.getText().trim());
                int gameDurationInSeconds = Integer.parseInt(timeField.getText().trim()) * 60;
                int bonusCount = Integer.parseInt(bonusField.getText().trim());

                if (size < 3 || size > 11 || size % 2 == 0) {
                    showError(errorLabel, "Boardgröße muss eine ungerade Zahl zwischen 3 und 11 sein!");
                    return;
                }
                if (treasuresPerPlayer < 1) {
                    showError(errorLabel, "Schatzkarten müssen >= 1 sein!");
                    return;
                }

                onConfirm.accept(new GameStartConfig(size, treasuresPerPlayer, gameDurationInSeconds, bonusCount));
                stage.close();
            } catch (NumberFormatException ex) {
                showError(errorLabel, "Bitte gültige Zahlen eingeben!");
            }
        };

        start.setOnAction(e -> confirm.run());

        // ENTER / ESC
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> start.fire();
                case ESCAPE -> cancel.fire();
            }
        });

        Platform.runLater(() -> {
            sizeField.requestFocus();
        });

        sizeField.setOnAction(e -> {
            if (sizeField.getText().trim().isEmpty()) sizeField.setText("7");
            if (sizeField.getText().contains("-")) {
                sizeField.setText("7");
                treasureField.requestFocus();
            }
            treasureField.requestFocus();
        });

        treasureField.setOnAction(e -> {
            if (treasureField.getText().trim().isEmpty()) treasureField.setText("3");
            timeField.requestFocus();
        });

        timeField.setOnAction(e -> {
            if (timeField.getText().trim().isEmpty()) timeField.setText("6");
            bonusField.requestFocus();
        });

        bonusField.setOnAction(e -> {
            if (bonusField.getText().trim().isEmpty()) bonusField.setText("4");
            start.fire();
        });

        stage.showAndWait();
    }
}
