package com.uni.labyrinth.ui;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HelpDialog {

    private final List<Image> images = new ArrayList<>();
    private int currentIndex = 0;

    public HelpDialog() {
        loadImages();
    }

    // Methode zum Öffnen des Anleitung-Fensters
    public void open() {
        Stage stage = new Stage();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/book.png")));
        stage.setTitle("Spielanleitung");

        BackgroundLayout bg = new BackgroundLayout();

        Label headline = new Label("Spielanleitung");
        headline.setFont(Font.font("Super Mario 256", 24));

        ImageView imageView = new ImageView(images.get(currentIndex));
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        Button backButton = new Button("Zurück");
        Button nextButton = new Button("Weiter");

        backButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == images.size() - 1);

        // Button Events
        backButton.setOnAction(e -> {
            if (currentIndex > 0) {
                currentIndex--;
                imageView.setImage(images.get(currentIndex));
                backButton.setDisable(currentIndex == 0);
                nextButton.setDisable(false);
            }
        });

        backButton.setStyle("-fx-background-color:  #706f6f;-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 15px;");
        backButton.setOnMouseEntered(e -> backButton.setCursor(Cursor.HAND));
        backButton.setOnMouseExited(e -> backButton.setCursor(Cursor.DEFAULT));

        backButton.setId("backButton");
        backButton.setPrefWidth(90);

        nextButton.setOnAction(e -> {
            if (currentIndex < images.size() - 1) {
                currentIndex++;
                imageView.setImage(images.get(currentIndex));
                nextButton.setDisable(currentIndex == images.size() - 1);
                backButton.setDisable(false);
            }
        });

        nextButton.setStyle("-fx-background-color:  #706f6f;-fx-text-fill: black; -fx-font-family: 'Super Mario 256'; -fx-font-size: 15px;");
        nextButton.setOnMouseEntered(e -> nextButton.setCursor(Cursor.HAND));
        nextButton.setOnMouseExited(e -> nextButton.setCursor(Cursor.DEFAULT));

        nextButton.setId("nextButton");
        backButton.setPrefWidth(90);

        HBox buttonsBox = new HBox(10, backButton, nextButton);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, headline, imageView, buttonsBox);
        layout.setStyle("-fx-padding: 20px 20px 60px 20px; -fx-alignment: center;");

        StackPane root = new StackPane();
        root.getChildren().addAll(bg.getBackground(), layout);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Lade die Anleitung-Bilder
    private void loadImages() {
        for (int i = 0; i < 11; i++) {
            String path = "/images/anleitung" + i + ".png";
            images.add(new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
        }
    }
}
