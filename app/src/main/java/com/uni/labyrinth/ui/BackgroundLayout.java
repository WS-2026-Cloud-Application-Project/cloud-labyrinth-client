package com.uni.labyrinth.ui;

import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BackgroundLayout {

    private final VBox background;

    public BackgroundLayout() {
        background = new VBox();
        background.setFillWidth(true);        // <-- wichtig
        background.setPrefHeight(VBox.USE_COMPUTED_SIZE);
        background.setMinHeight(0);

        // Wachsender Bereich
        Region growingBackground = new Region();
        growingBackground.setStyle("-fx-background-color: rgb(180,215,255);");

        // Mindesthöhe 0 erlaubt Wachstum
        growingBackground.setMinHeight(0);
        growingBackground.setPrefHeight(0);

        VBox.setVgrow(growingBackground, Priority.ALWAYS);

        // Untere Streifen
        Rectangle r2 = new Rectangle();
        Rectangle r3 = new Rectangle();
        Rectangle r4 = new Rectangle();
        Rectangle r5 = new Rectangle();

        r2.setHeight(8);
        r3.setHeight(6);
        r4.setHeight(4);
        r5.setHeight(25);

        r2.setFill(Color.rgb(90, 190, 50));
        r3.setFill(Color.rgb(120, 235, 90));
        r4.setFill(Color.rgb(40, 120, 20));
        r5.setFill(Color.rgb(210, 185, 80));

        background.getChildren().addAll(growingBackground, r2, r3, r4, r5);

        // Breite binden
        growingBackground.prefWidthProperty().bind(background.widthProperty());
        r2.widthProperty().bind(background.widthProperty());
        r3.widthProperty().bind(background.widthProperty());
        r4.widthProperty().bind(background.widthProperty());
        r5.widthProperty().bind(background.widthProperty());
    }

    public VBox getBackground() {
        return background;
    }
}
