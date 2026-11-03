package com.uni.labyrinth.ui;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.service.GameServerService;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class MenuPopup {

    private final Popup popup = new Popup();

    public MenuPopup(Node owner, GameServerService gameServerService, Main mainApp) {
        Button disconnectBtn = new Button("Disconnect");
        Button helpBtn = new Button("Anleitung");

        HelpDialog helpDialog = new HelpDialog();

        disconnectBtn.setId("disconnectBtn");
        helpBtn.setId("helpBtn");

        VBox content = new VBox(10, helpBtn, disconnectBtn);
        content.setStyle("""
                    -fx-padding: 10;
                    -fx-background-color: #1e1e1e;
                    -fx-background-radius: 10;
                """);

        popup.getContent().add(content);
        popup.setAutoHide(true);

        disconnectBtn.setOnAction(e -> {
            popup.hide();
            mainApp.getLobbyState().getMyPlayer().setAiControlled(false);
            gameServerService.sendDisconnectCommand(mainApp);
        });
        disconnectBtn.setStyle("-fx-background-color:  #971919; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 15px;");
        disconnectBtn.setOnMouseEntered(e -> disconnectBtn.setCursor(Cursor.HAND));
        disconnectBtn.setOnMouseExited(e -> disconnectBtn.setCursor(Cursor.DEFAULT));



        disconnectBtn.setId("disconnectButton");
        disconnectBtn.setPrefWidth(130);

        helpBtn.setOnAction(e -> {
            popup.hide();
            helpDialog.open();
        });
        helpBtn.setStyle("-fx-background-color:  #706f6f;-fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 15px;");
        helpBtn.setOnMouseEntered(e -> helpBtn.setCursor(Cursor.HAND));
        helpBtn.setOnMouseExited(e -> helpBtn.setCursor(Cursor.DEFAULT));
        helpBtn.setId("helpButton");
        helpBtn.setPrefWidth(130);
    }

    public void show(Node owner) {
        if (popup.isShowing()) return;

        Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
        popup.show(owner, bounds.getMinX(), bounds.getMaxY());
    }
}
