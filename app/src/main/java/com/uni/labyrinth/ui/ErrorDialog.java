package com.uni.labyrinth.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ErrorDialog {

    public static void show(Stage owner, String titleText, String headerText, String messageText) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        // ===== Top Bar =====
        HBox topBar = new HBox(8);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8));
        topBar.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 20 20 0 0;");

        // Icon links
        ImageView icon = new ImageView(new Image(ErrorDialog.class.getResourceAsStream("/images/cloud.png")));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);

        // Spacer schiebt alles auseinander
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Text rechts (rechtsbündig)
        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 15px;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-size: 18px;
                """);

        topBar.getChildren().addAll(icon, spacer, title, closeBtn);

        closeBtn.setOnAction(e -> stage.close());

        // ===== Content =====
        Label header = new Label(headerText);
        header.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Super Mario 256'; -fx-font-size: 20px;");

        Label message = new Label(messageText != null ? messageText : "");
        message.setWrapText(true);          // Text darf umbrechen
        message.setMaxWidth(350);           // Breite begrenzen (z.B. 350px)
        message.setPrefWidth(350);
        message.setMinWidth(350);
        message.setStyle("-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 16px;");

        Button ok = new Button("OK");
        ok.setStyle("-fx-background-color: #ffb3b3; -fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 18px;");
        ok.setOnAction(e -> stage.close());

        HBox buttons = new HBox(ok);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, header, message, buttons);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 0 0 20 20;");

        VBox root = new VBox(topBar, content);
        root.setStyle("""
                    -fx-background-color: #1e1e1e;
                    -fx-background-radius: 20;
                """);

        // ===== Runde Ecken =====
        Rectangle clip = new Rectangle();
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);

        // ===== Shadow =====
        DropShadow shadow = new DropShadow();
        shadow.setRadius(25);
        shadow.setSpread(0.2);
        shadow.setOffsetY(8);
        shadow.setColor(Color.rgb(0, 0, 0, 0.6));
        root.setEffect(shadow);

        // ===== Scene =====
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // ENTER & ESC schließen
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, ESCAPE -> ok.fire();
            }
        });

        stage.showAndWait();
    }
}
