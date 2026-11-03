package com.uni.labyrinth.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class UsernameDialog {

    public static Optional<String> show(Stage owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT); // wichtig für abgerundete Ecken

        // ===== "Titelleiste" selbst bauen =====
        Label title = new Label("Login");
        title.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 15px;");

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

        closeBtn.setOnMouseEntered(e -> closeBtn.setCursor(Cursor.HAND));
        closeBtn.setOnMouseExited(e -> closeBtn.setCursor(Cursor.DEFAULT));
        closeBtn.setOnAction(e -> stage.close());

        // ===== Inhalt =====
        Label label = new Label("Benutzername:");
        label.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        TextField input = new TextField();
        input.setStyle("-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        input.setPromptText("Name eingeben...");

        Button ok = new Button("OK");
        ok.setStyle("-fx-background-color: #4CAF50;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        ok.setOnMouseEntered(e -> ok.setCursor(Cursor.HAND));
        ok.setOnMouseExited(e -> ok.setCursor(Cursor.DEFAULT));



        Button cancel = new Button("Abbrechen");
        cancel.setStyle("-fx-background-color:  #971919;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");
        cancel.setOnMouseEntered(e -> cancel.setCursor(Cursor.HAND));
        cancel.setOnMouseExited(e -> cancel.setCursor(Cursor.DEFAULT));


        HBox buttons = new HBox(10, ok, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(10, label, input, buttons);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 0 0 20 20;");

        VBox root = new VBox(topBar, content);

        // ===== Runde Ecken =====
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

        final String[] result = {null};

        ok.setOnAction(e -> {
            result[0] = input.getText();
            stage.close();
        });

        cancel.setOnAction(e -> {
            result[0] = null;
            stage.close();
        });

        // ENTER = OK, ESC = Cancel
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> ok.fire();
                case ESCAPE -> cancel.fire();
            }
        });

        stage.setOnShown(e -> input.requestFocus());
        stage.showAndWait();
        return Optional.ofNullable(result[0]).filter(s -> !s.isBlank());
    }
}
