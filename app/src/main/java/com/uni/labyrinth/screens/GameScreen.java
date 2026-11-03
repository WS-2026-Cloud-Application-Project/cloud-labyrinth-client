package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.enums.BonusType;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.domain.enums.TurnState;
import com.uni.labyrinth.model.*;
import com.uni.labyrinth.service.AIConfig;
import com.uni.labyrinth.service.AIServiceSafe;
import com.uni.labyrinth.service.GameServerService;
import com.uni.labyrinth.service.SafeMoveStrategy;
import com.uni.labyrinth.service.timemeasurement.LapTimer;
import com.uni.labyrinth.service.timemeasurement.TimeParsing;
import com.uni.labyrinth.ui.BackgroundLayout;
import com.uni.labyrinth.ui.GameBoardView;
import com.uni.labyrinth.ui.MenuPopup;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.uni.labyrinth.domain.enums.BonusType.*;
import static com.uni.labyrinth.ui.GameBoardView.PushButtonMode.ALL;

public class GameScreen {

    private static final int MAX_AI_RETRIES = 3;   // Max Retries bei Fehlern
    private final GameState gameState;
    private final GameServerService gameServerService;
    private final int totalTreasures;
    private final StackPane treasureView = new StackPane();
    private final ImageView remainingTreasures = new ImageView();
    private final HBox messagePane = new HBox(10);
    private final Label messageLabel = new Label();
    private final Region leftSpacer = new Region();
    private final Region rightSpacer = new Region();
    private final ImageView leftMsgIcon = new ImageView();
    private final ImageView rightMsgIcon = new ImageView();
    private final int tileSize = 70;
    private final EnumMap<BonusType, VBox> bonusButtons = new EnumMap<>(BonusType.class);
    private final Main mainApp;
    private final Label myTurnLabel = new Label("");
    private final ScheduledExecutorService turnTimerExecutor;
    private Pane root;
    private GameBoardView gameBoardView;
    private StackPane extraTile;
    private int treasuresFound;
    private Label treasuresFoundAndTotal;
    private ImageView currentTreasure;
    private PauseTransition messageTimer;
    private Circle pawnColor;
    private BonusType activeBonusType;
    // AI-Modus Attribute
    private AIServiceSafe aiService;
    private volatile boolean aiTurnInProgress = false;      // volatile: thread-safe
    private volatile boolean aiWaitingForMove = false;      // Neue Phase: Warte auf Move nach Push (volatile: thread-safe)
    private volatile boolean aiWaitingForSecondPush = false; // PUSH_TWICE: Warte auf zweiten Push (volatile: thread-safe)
    private AIDecision currentAIDecision = null;    // Speichert Decision für zweiten Push
    private volatile boolean aiAutoMode = false;   // Auto-Modus: KI spielt automatisch alle Züge (volatile: thread-safe)
    private String lastProcessedTurnId = null;     // Verhindert mehrfache Trigger
    private int aiRetryCount = 0;                  // Zählt Retry-Versuche
    private volatile boolean isGameOver = false;   // Flag: Spiel ist beendet (volatile: thread-safe für WebSocket-Thread)
    private ScheduledExecutorService aiExecutor;
    private Button aiButton;
    private ToggleButton aiAutoButton;
    private VBox playerListBox;
    // AFK-Feature Attribute
    private boolean afkModeActive = false;     // True wenn Spieler AFK ist (Auto-KI wegen Timeout)
    private Button imBackButton;               // "Ich bin zurück" Button
    private java.util.concurrent.ScheduledFuture<?> currentTurnTimer;
    private long lastPlayerActionTime = 0;     // Timestamp der letzten manuellen Aktion
    private boolean wasMyTurnBefore = false;   // War es vorher mein Zug?
    private long turnStartTime = 0;            // Wann hat mein Zug gestartet?

    private Label debugStatusLabel;
    private String originalServerUri; // Speichert die ursprüngliche Server-URI
    private String savedIdentifierToken; // Speichert den IdentifierToken für Reconnect

    private final ImageView rotateButton = new ImageView(new Image(getClass().getResourceAsStream("/images/rotate.png")));

    public GameScreen(GameState gameState, GameServerService gameServerService, Main mainApp) {
        this.gameState = gameState;
        this.mainApp = mainApp;
        this.gameServerService = gameServerService;
        PlayerState thisState = gameState.getMyPlayerState();
        int reamining = thisState.getRemainingTreasureCount();
        this.treasuresFound = gameState.getMyPlayerState().getTreasuresFoundCount();
        this.totalTreasures = reamining + this.treasuresFound;

        // AI Service initialisieren (Konfiguration aus AIConfig)
        if (AIConfig.isAvailable()) {
            this.aiService = new AIServiceSafe(AIConfig.getApiKey());
            this.aiExecutor = Executors.newSingleThreadScheduledExecutor();
            System.out.println("   Model: " + AIConfig.getModel());
        } else {
            System.out.println("WARNUNG: Kein API-Key verfügbar - AI-Modus nicht verfügbar");
        }

        // Turn Timer Executor für AFK-Feature
        this.turnTimerExecutor = Executors.newSingleThreadScheduledExecutor();

        // Speichere Server-URI und IdentifierToken für Debug-Reconnect
        try {
            if (gameServerService.getCurrentSession() != null && gameServerService.getCurrentSession().isOpen()) {
                this.originalServerUri = gameServerService.getCurrentSession().getRequestURI().toString();
                System.out.println(" Server-URI gespeichert beim Start: " + originalServerUri);
            }

            // Speichere IdentifierToken aus LobbyState
            if (mainApp.getLobbyState() != null && mainApp.getLobbyState().getIdentifierToken() != null) {
                this.savedIdentifierToken = mainApp.getLobbyState().getIdentifierToken();
                System.out.println(" IdentifierToken gespeichert beim Start: " + savedIdentifierToken);
            }
        } catch (Exception e) {
            System.err.println(" Konnte Daten nicht beim Start speichern: " + e.getMessage());
        }

        initUI(gameServerService);

        gameState.addListener(gs -> javafx.application.Platform.runLater(() -> updateFromGameState(gs, gameServerService)));
    }

    private void initUI(GameServerService gameServerService) {
        BackgroundLayout bg = new BackgroundLayout();
        VBox ui = new VBox(10);
        ui.setAlignment(Pos.CENTER);
        ui.setPadding(new Insets(25, 0, 50, 0));

        if (gameState.getBoard() != null && gameState.getBoard().length > 0) {
            gameBoardView = new GameBoardView(gameState);
            gameBoardView.drawAllPawns(gameState.getPlayers());
        } else {
            gameBoardView = new GameBoardView(null);
        }

        gameBoardView.setId("gameBoardView");
        gameBoardView.addPushButtons(gameServerService);

        gameBoardView.setTileClickListener((column, row) -> {
            if (isGameOver) return; // Keine Aktionen nach Game Over
            PlayerState myPlayer = gameState.getMyPlayerState();
            if (myPlayer != null) {
                Coordinates coordinates = new Coordinates(column, row);
                onPlayerAction(); // Spieler macht eine Aktion!

                if (activeBonusType == null) {
                    gameServerService.sendMovePlayer(coordinates);
                } else if (activeBonusType == BEAM) {
                    gameServerService.sendBeamCommand(coordinates);
                    setActiveBonusType(null);
                } else if (activeBonusType == PUSH_FIXED) {
                    gameBoardView.setActiveBonus(PUSH_FIXED);
                }
            }
        });

        gameBoardView.setPawnClickListener(playerId -> {
            if (isGameOver) return; // Keine Aktionen nach Game Over
            if (activeBonusType == SWAP) {
                onPlayerAction(); // Spieler macht eine Aktion!
                gameServerService.sendSwapCommand(playerId);
            }
        });

        VBox controls = createControls(gameState);
        controls.setAlignment(Pos.TOP_LEFT);

        HBox extraTileBox = new HBox(10);
        extraTile = new StackPane(gameBoardView.createTileImage(gameState.getExtraTile(), tileSize));
        rotateButton.setId("rotateButton");
        rotateButton.setFitWidth(tileSize);
        rotateButton.setFitHeight(tileSize);
        rotateButton.setPickOnBounds(true);
        rotateButton.setOnMouseEntered(e -> rotateButton.setCursor(Cursor.HAND));
        rotateButton.setOnMouseExited(e -> rotateButton.setCursor(Cursor.DEFAULT));
        rotateButton.setOnMouseClicked(event -> {
            if (isGameOver) return; // Keine Aktionen nach Game Over
            onPlayerAction(); // Spieler macht eine Aktion!
            gameServerService.sendRotateCommand();
        });

        MenuPopup menuPopup = new MenuPopup(this.getRoot(), gameServerService, mainApp);

        ImageView menuButton = new ImageView(new Image(getClass().getResourceAsStream("/images/menu.png")));
        menuButton.setId("menuButton");
        menuButton.setFitWidth(tileSize);
        menuButton.setFitHeight(tileSize);
        menuButton.setPickOnBounds(true);
        menuButton.setOnMouseEntered(e -> menuButton.setCursor(Cursor.HAND));
        menuButton.setOnMouseExited(e -> menuButton.setCursor(Cursor.DEFAULT));
        menuButton.setOnMouseClicked(event -> {
            menuPopup.show(menuButton);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        myTurnLabel.setAlignment(Pos.CENTER);
        pawnColor = new Circle(9);
        pawnColor.setVisible(false);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        extraTileBox.getChildren().addAll(extraTile, rotateButton, spacer, myTurnLabel, pawnColor, spacer2, menuButton);
        extraTileBox.setAlignment(Pos.CENTER);

        initMessagePane(); //  initialisieren (aber NICHT an root hängen!)
        messagePane.setMinHeight(80);
        messagePane.setAlignment(Pos.CENTER);
        VBox rightColumn = new VBox(10);
        VBox.setMargin(messagePane, new Insets(10, 0, 0, 0));
        rightColumn.setAlignment(Pos.TOP_CENTER);

        rightColumn.getChildren().addAll(extraTileBox, gameBoardView, messagePane);
        HBox board = new HBox(controls, rightColumn);
        board.setSpacing(30);
        board.setAlignment(Pos.TOP_CENTER);

        ui.getChildren().addAll(board);

        VBox overallBox = new VBox(10);
        overallBox.setAlignment(Pos.TOP_CENTER);
        overallBox.setPadding(new Insets(20, 0, 20, 0));
        Label screenHeader = new Label("Mario's verrücktes Labyrinth");
        screenHeader.setId("screenHeader");
        screenHeader.setFont(Font.font("Super Mario 256", 30));
        overallBox.getChildren().addAll(screenHeader, ui);

        root = new StackPane(bg.getBackground(), overallBox);
    }

    private VBox createControls(GameState gameState) {
        VBox controls = new VBox();
        controls.setPrefSize(370, 350);
        VBox.setMargin(controls, new Insets(10));
        controls.setSpacing(15);
        controls.setAlignment(Pos.TOP_LEFT);

        Label bonusText = new Label("Bonus");
        bonusText.setFont(Font.font("Super Mario 256", 20));

        HBox bonusButtonBox = createBonusButtons();

        Label kiText = new Label("KI-Hilfe");
        kiText.setFont(Font.font("Super Mario 256", 24));

        // Einzelner KI-Zug Button
        aiButton = new Button("KI-Zug");
        aiButton.setId("aiButton");
        aiButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");
        aiButton.setDisable(false); // Immer aktiv (außer AI Service fehlt oder Auto-KI läuft)

        aiButton.setOnMouseEntered(e -> aiButton.setCursor(Cursor.HAND));
        aiButton.setOnMouseExited(e -> aiButton.setCursor(Cursor.DEFAULT));

        // Auto-KI Button (Toggle)
        aiAutoButton = new ToggleButton("Auto-KI: AUS");
        aiAutoButton.setId("aiAutoButton");
        aiAutoButton.setStyle("-fx-background-color: #971919; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");

        aiAutoButton.setDisable(true);

        aiAutoButton.setOnMouseEntered(e -> aiAutoButton.setCursor(Cursor.HAND));
        aiAutoButton.setOnMouseExited(e -> aiAutoButton.setCursor(Cursor.DEFAULT));


        // AI Service nicht verfügbar?
        if (aiService == null) {
            aiButton.setText("KI nicht verfügbar");
            aiButton.setDisable(true);
            aiButton.setOpacity(0.5);
            aiAutoButton.setText("Auto-KI nicht verfügbar");
            aiAutoButton.setOpacity(0.5);
        } else {
            aiButton.setDisable(false);
            aiButton.setOpacity(1.0);
            aiAutoButton.setDisable(false);
            aiAutoButton.setOpacity(1.0);
        }

        // Einzelzug-Button Handler
        aiButton.setOnAction(event -> {
            if (isGameOver) {
                System.out.println(" KI-Zug nicht möglich - Spiel ist beendet");
                return;
            }
            if (aiService != null && !aiTurnInProgress && !aiAutoMode) {
                System.out.println("KI-Zug wurde angefordert");
                executeAITurn(gameServerService);
            } else if (aiAutoMode) {
                System.out.println(" Einzelner KI-Zug nicht möglich - Auto-KI ist aktiv");
            }
        });

        // Auto-Modus Toggle Handler
        aiAutoButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            // Prüfe ob Spiel vorbei ist UND Button wird aktiviert (nicht deaktiviert)
            // Bei Deaktivierung während Game Over erlauben (für cleanup)
            if (isGameOver && isSelected) {
                System.out.println(" Auto-KI Aktivierung nicht möglich - Spiel ist beendet");
                Platform.runLater(() -> aiAutoButton.setSelected(false));
                return;
            }

            // Wenn aiAutoMode schon den gewünschten Wert hat, nichts tun
            // Verhindert doppelte Ausführung bei programmatischem setSelected()
            if (aiAutoMode == isSelected) {
                return;
            }

            aiAutoMode = isSelected;
            if (isSelected) {
                aiAutoButton.setText("Auto-KI: AN");
                aiButton.setVisible(false);
                aiButton.setManaged(false);
                aiAutoButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");

                System.out.println(" Auto-KI Modus aktiviert - KI läuft CLIENT-seitig!");

                // Einzelzug-Button deaktivieren im Auto-Modus
                if (aiService != null) {
                    aiButton.setDisable(true);
                    aiButton.setStyle("-fx-background-color: #D3D3D3; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");
                }

                // Prüfe ob ich aktuell am Zug bin und führe sofort einen KI-Zug aus
                boolean isMyTurn = gameState.getCurrentPlayerId() != null && gameState.getMyPlayerState() != null && gameState.getMyPlayerState().getPlayerInfo() != null && gameState.getCurrentPlayerId().equals(gameState.getMyPlayerState().getPlayerInfo().getId());

                if (isMyTurn && aiService != null && !aiTurnInProgress) {
                    System.out.println(" Auto-KI aktiviert und Spieler ist am Zug → Führe sofort KI-Zug aus");
                    executeAITurn(gameServerService);
                } else {
                    System.out.println(" Auto-KI aktiviert, aber nicht am Zug → Warte auf nächsten Zug");
                }

            } else {
                aiAutoButton.setText("Auto-KI: AUS");
                aiAutoButton.setStyle("-fx-background-color: #971919; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");
                aiButton.setVisible(true);
                aiButton.setManaged(true);
                System.out.println("⏸  Auto-KI Modus deaktiviert");

                // Einzelzug-Button wieder aktivieren wenn Auto-KI aus ist
                if (aiService != null) {
                    aiButton.setDisable(false);
                }
            }
        });

        // "Ich bin zurück" Button (initial versteckt)
        imBackButton = new Button("Bin zurück");
        imBackButton.setId("imBackButton");
        imBackButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");
        imBackButton.setVisible(false); // Initial versteckt
        imBackButton.setManaged(false); // Nimmt keinen Platz weg wenn versteckt

        imBackButton.setOnMouseEntered(e -> imBackButton.setCursor(Cursor.HAND));
        imBackButton.setOnMouseExited(e -> imBackButton.setCursor(Cursor.DEFAULT));
        imBackButton.setOnAction(event -> {
            System.out.println(" Spieler ist zurück - deaktiviere AFK-Modus");
            deactivateAFKMode();
        });

        // HBox für alle AI-Buttons
        HBox aiButtonBox = new HBox(10);
        aiButtonBox.getChildren().addAll(aiButton, aiAutoButton, imBackButton);

        // HBox für Debug Buttons
        //VBox debugButtonBox = createDebugButtons();
        // Treasure Box
        VBox treasureBox = createTreasureBox();
        // Spieler-Liste
        VBox playerListBox = createPlayerListBox();

        controls.getChildren().addAll(playerListBox, bonusText, bonusButtonBox, kiText, aiButtonBox,
                //debugButtonBox,
                treasureBox);
        return controls;
    }

    private VBox createTreasureBox() {
        Label treasureHeadline = new Label("Schätze");
        treasureHeadline.setFont(Font.font("Super Mario 256", 24));

        treasuresFoundAndTotal = new Label(treasuresFound + " / " + totalTreasures);
        treasuresFoundAndTotal.setId("treasuresFoundAndTotalLabel");
        treasuresFoundAndTotal.setFont(Font.font("Press Start 2P", 12));

        HBox treasureText = new HBox(15, treasureHeadline, treasuresFoundAndTotal);
        treasureText.setAlignment(Pos.BOTTOM_LEFT);

        if (gameState.getMyPlayerState().getRemainingTreasureCount() > 6) {
            remainingTreasures.setImage(new Image(getClass().getResourceAsStream("/images/remaining_treasures.png")));
        } else {
            int count = gameState.getMyPlayerState().getRemainingTreasureCount();
            if (count > 5) count = 5;
            if (count < 0) count = 0;
            remainingTreasures.setImage(new Image(getClass().getResourceAsStream("/images/remaining_treasures" + count + ".png")));
        }

        if (gameState.getMyPlayerState().getCurrentTreasure() != null) {
            int id = gameState.getMyPlayerState().getCurrentTreasure().getId();
            if (id > 0 && id <= 24) {
                currentTreasure = new ImageView(new Image(getClass().getResourceAsStream("/images/treasure" + id + ".png")));
            } else {
                currentTreasure = new ImageView(new Image(getClass().getResourceAsStream("/images/treasure.png")));
            }
        } else {
            currentTreasure = new ImageView(new Image(getClass().getResourceAsStream("/images/treasure.png")));
        }

        treasureView.getChildren().clear();
        treasureView.getChildren().addAll(remainingTreasures, currentTreasure);

        treasureView.setPrefSize(350, 350);
        treasureView.setPadding(new Insets(15, 0, 0, 0));

        remainingTreasures.setFitWidth(350);
        remainingTreasures.setPreserveRatio(true);

        currentTreasure.setFitWidth(100);
        currentTreasure.setPreserveRatio(true);

        VBox treasureBox = new VBox(5);
        treasureBox.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(treasureView, Priority.NEVER);

        treasureBox.getChildren().addAll(treasureText, treasureView);
        return treasureBox;
    }

    private VBox createPlayerListBox() {
        // Spieler-Liste
        Label playerListHeadline = new Label("Spieler");
        playerListHeadline.setFont(Font.font("Super Mario 256", 20));

        playerListBox = new VBox(5);
        playerListBox.setAlignment(Pos.TOP_LEFT);
        playerListBox.setPrefHeight(120);
        playerListBox.setStyle("""
                 -fx-background-color: transparent;
                    //-fx-border-color: rgba(255, 255, 255, 0.6);
                    -fx-border-width: 0;
                    -fx-border-radius: 8;
                    -fx-background-radius: 8;
                    -fx-padding: 10;
                """);

        updatePlayerList(gameState.getPlayers());
        return new VBox(5, playerListHeadline, playerListBox);
    }

    private HBox createBonusButtons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);

        for (BonusType type : BonusType.values()) {

            double size = 45;
            Circle circle = new Circle(size / 2, Color.LIGHTGRAY);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            circle.setOpacity(0.6);

            Label label;

            switch (type) {
                case BEAM -> label = new Label("Beamen");
                case SWAP -> label = new Label("Tauschen");
                case PUSH_FIXED -> label = new Label("Fest\nSchieben");
                case PUSH_TWICE -> label = new Label("Doppelt\nSchieben");
                default -> label = new Label("Unbekannt");
            }

            label.setFont(Font.font("Press Start 2P", 10));
            label.setTextFill(Color.BLACK);
            label.setOpacity(1);
            label.setMinHeight(40);
            label.setWrapText(true);
            label.setTextAlignment(TextAlignment.CENTER);

            VBox button = new VBox(2, circle, label);
            button.setUserData(type);
            button.setAlignment(Pos.CENTER);
            button.setPrefSize(85, 60);
            button.setId("bonusButton" + type);
            button.setDisable(true);

            bonusButtons.put(type, button);
            box.getChildren().add(button);
        }

        return box;
    }

    private void updateButtons(List<BonusType> availableBonuses, GameServerService gameServerService) {
        Map<BonusType, Integer> bonusCounts = new EnumMap<>(BonusType.class);
        if (availableBonuses != null) {
            for (BonusType type : availableBonuses) {
                bonusCounts.put(type, bonusCounts.getOrDefault(type, 0) + 1);
            }
        }

        // alles deaktivieren
        for (VBox button : bonusButtons.values()) {
            Circle circle = getCircleFromButton(button);
            Label label = getLabelFromButton(button);

            // evtl. vorhandenen Counter entfernen
            button.getChildren().removeIf(n -> n instanceof StackPane);

            if (circle != null && !button.getChildren().contains(circle)) {
                button.getChildren().add(0, circle);
            }

            BonusType type = (BonusType) button.getUserData();
            if (type != null) {
                switch (type) {
                    case BEAM -> label.setText("Beamen");
                    case SWAP -> label.setText("Tauschen");
                    case PUSH_FIXED -> label.setText("Fest\nSchieben");
                    case PUSH_TWICE -> label.setText("Doppelt\nSchieben");
                }
            }

            circle.setFill(Color.LIGHTGRAY);
            circle.setStrokeWidth(2);
            circle.setOpacity(0.6);

            label.setOpacity(0.6);
            label.setTextFill(Color.BLACK);
            label.setWrapText(true);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setAlignment(Pos.CENTER);

            button.setAlignment(Pos.CENTER);
            button.setDisable(true);
            button.setOnMouseClicked(null);
        }

        if (availableBonuses == null) return;

        // Boni aktivieren
        for (BonusType type : bonusCounts.keySet()) {
            VBox button = bonusButtons.get(type);
            if (button == null) continue;

            Circle circle = getCircleFromButton(button);
            Label label = getLabelFromButton(button);

            circle.setFill(Color.GOLD);
            circle.setStrokeWidth(2);
            label.setTextFill(Color.BLACK);
            button.setDisable(false);
            circle.setOpacity(1.0);
            label.setOpacity(1.0);
            button.setStyle("-fx-cursor: hand;");
            button.setAlignment(Pos.CENTER);

            int count = bonusCounts.getOrDefault(type, 1);

            if (count > 1) {
                // alten Circle aus der VBox entfernen
                button.getChildren().remove(circle);

                Text countText = new Text(count + "x");
                countText.setStyle("-fx-font-weight: bold;");
                countText.setFont(Font.font("Press Start 2P", 9));

                StackPane stack = new StackPane(circle);
                stack.setMinSize(circle.getRadius() * 2, circle.getRadius() * 2);
                stack.setPrefSize(circle.getRadius() * 2, circle.getRadius() * 2);

                StackPane.setAlignment(countText, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(countText, new Insets(0, 0, 2, 0));

                stack.getChildren().add(countText);

                // StackPane an Position 0 einfügen
                button.getChildren().add(0, stack);
            }

            button.setOnMouseClicked(e -> {
                if (isGameOver) return; // Keine Aktionen nach Game Over
                boolean isMyTurn = Objects.equals(gameState.getCurrentPlayerId(), gameState.getMyPlayerState().getPlayerInfo().getId());
                boolean isMyPush = isMyTurn && gameState.getCurrentTurnState() == TurnState.WAITING_FOR_PUSH;
                switch (type) {
                    case BEAM -> {
                        if (isMyTurn && isMyPush) {
                            boolean active = toggleActiveBonusType(BEAM);
                            if (active) {
                                circle.setStrokeWidth(5);
                            } else {
                                circle.setStrokeWidth(2);
                            }
                        } else {
                            Platform.runLater(() -> {
                                final String message;
                                if (isMyTurn && !isMyPush) {
                                    message = "Reihenfolge: Erst Beamen dann laufen.";
                                } else {
                                    message = " Du bist nicht am Zug.";
                                }
                                Stage stage = (Stage) Stage.getWindows().get(0);
                                showError("Beamen nicht erlaubt: \n" + message);
                            });
                        }
                    }
                    case SWAP -> {
                        if (isMyTurn && isMyPush) {
                            boolean active = toggleActiveBonusType(SWAP);
                            if (active) {
                                circle.setStrokeWidth(5);
                            } else {
                                circle.setStrokeWidth(2);
                            }
                            circle.setStrokeWidth(5);

                        } else {
                            Platform.runLater(() -> {
                                final String message;
                                if (isMyTurn && !isMyPush) {
                                    message = "Reihenfolge: Erst Tauschen dann laufen.";
                                } else {
                                    message = " Du bist nicht am Zug.";
                                }
                                Stage stage = (Stage) Stage.getWindows().get(0);
                                showError("Tauschen nicht erlaubt: \n" + message);
                            });
                        }
                    }
                    case PUSH_FIXED -> {
                        if (isMyTurn && isMyPush) {
                            gameBoardView.setActiveBonus(PUSH_FIXED);
                            circle.setStrokeWidth(5);
                            gameBoardView.setPushButtonMode(ALL);
                        } else {
                            Platform.runLater(() -> {
                                final String message;
                                if (isMyTurn) {
                                    message = "Jetzt ist laufen angesagt.";
                                } else {
                                    message = " Du bist nicht am Zug.";
                                }
                                Stage stage = (Stage) Stage.getWindows().get(0);
                                showError("Feste Plättchen schieben nicht erlaubt: \n" + message);
                            });
                        }
                    }
                    case PUSH_TWICE -> {
                        if (isMyTurn && isMyPush) {
                            onPlayerAction(); // Spieler macht eine Aktion!
                            circle.setStrokeWidth(5);
                            gameServerService.sendPushTwiceCommand();
                        } else {
                            Platform.runLater(() -> {
                                final String message;
                                if (isMyTurn) {
                                    message = "Jetzt ist laufen angesagt.";
                                } else {
                                    message = " Du bist nicht am Zug.";
                                }
                                Stage stage = (Stage) Stage.getWindows().get(0);
                                showError("Doppelt schieben nicht erlaubt: " + message);
                            });
                        }
                    }
                }
            });
        }
    }

    /*
    private VBox createDebugButtons() {
        // Debug-Buttons für Disconnect/Reconnect Tests
        Label debugText = new Label("Debug");
        debugText.setFont(Font.font("Super Mario 256", 20));

        // Debug-Feature für Disconnect/Reconnect Tests
        Button debugDisconnectButton = new Button(" Disconnect");
        debugDisconnectButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8px 16px;");
        debugDisconnectButton.setOnAction(event -> {
            System.out.println(" DEBUG: Manueller Disconnect ausgelöst");

            // Speichere Server-URI vor dem Disconnect
            try {
                if (gameServerService.getCurrentSession() != null && gameServerService.getCurrentSession().isOpen()) {
                    originalServerUri = gameServerService.getCurrentSession().getRequestURI().toString();
                    System.out.println(" DEBUG: Server-URI gespeichert: " + originalServerUri);
                }
            } catch (Exception e) {
                System.err.println(" DEBUG: Konnte Server-URI nicht speichern: " + e.getMessage());
            }

            debugStatusLabel.setText("Status: Disconnected");
            debugStatusLabel.setStyle("-fx-text-fill: red;");
            gameServerService.disconnect();
        });

        Button debugReconnectButton = new Button(" Reconnect");
        debugReconnectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8px 16px;");
        debugReconnectButton.setOnAction(event -> {
            System.out.println(" DEBUG: Manueller Reconnect ausgelöst");
            debugReconnect();
        });

        debugStatusLabel = new Label("Status: Connected");
        debugStatusLabel.setFont(Font.font("Press Start 2P", 10));
        debugStatusLabel.setStyle("-fx-text-fill: green;");

        HBox debugButtonBox = new HBox(10);
        debugButtonBox.getChildren().addAll(debugDisconnectButton, debugReconnectButton, debugStatusLabel);

        VBox DebugBox= new VBox(5);
        DebugBox.setAlignment(Pos.TOP_LEFT);
        DebugBox.getChildren().addAll(debugText, debugButtonBox);
        return DebugBox;
    }

    private void debugReconnect() {
        // Warte kurz (500ms) um sicherzustellen, dass Disconnect vollständig ist
        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(this::performReconnect);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void performReconnect() {
        try {
            // Hole Username aus GameState (verfügbar während des Spiels)
            String username = null;
            String identifierToken = savedIdentifierToken; // Verwende gespeicherten Token

            // Versuche zuerst aus GameState (während des Spiels)
            if (gameState != null && gameState.getMyPlayerState() != null
                    && gameState.getMyPlayerState().getPlayerInfo() != null) {
                username = gameState.getMyPlayerState().getPlayerInfo().getName();
                System.out.println(" DEBUG: Username aus GameState: " + username);
            }

            // Fallback: Versuche aus LobbyState
            if (username == null && mainApp.getLobbyState() != null) {
                PlayerInfo myPlayer = mainApp.getLobbyState().getMyPlayer();
                if (myPlayer != null) {
                    username = myPlayer.getName();
                    System.out.println(" DEBUG: Username aus LobbyState: " + username);
                }
                if (identifierToken == null) {
                    identifierToken = mainApp.getLobbyState().getIdentifierToken();
                    System.out.println(" DEBUG: IdentifierToken aus LobbyState: " + identifierToken);
                }
            }

            if (username == null || username.isEmpty()) {
                System.err.println(" DEBUG: Kein Spielername gefunden (weder in GameState noch LobbyState)");
                debugStatusLabel.setText("Status: No Username");
                debugStatusLabel.setStyle("-fx-text-fill: orange;");
                return;
            }

            if (identifierToken == null || identifierToken.isEmpty()) {
                System.err.println(" DEBUG: Kein IdentifierToken gefunden");
                debugStatusLabel.setText("Status: No Token");
                debugStatusLabel.setStyle("-fx-text-fill: orange;");
                return;
            }

            System.out.println(" DEBUG: Reconnect als Benutzer: " + username);
            System.out.println(" DEBUG: Mit IdentifierToken: " + identifierToken);

            // Prüfe ob wir eine gespeicherte Server-URI haben
            if (originalServerUri == null || originalServerUri.isEmpty()) {
                System.err.println(" DEBUG: Keine Server-URI gespeichert. Bitte zuerst mit 'Disconnect' trennen.");
                debugStatusLabel.setText("Status: No URI saved");
                debugStatusLabel.setStyle("-fx-text-fill: orange;");
                return;
            }

            System.out.println(" DEBUG: Verwende gespeicherte URI: " + originalServerUri);

            // Erstelle GameServer mit gespeicherter URI
            GameServer server = new GameServer();
            server.setUri(originalServerUri);
            server.setName("Debug-Reconnect-Server");

            // Verbinde mit Server (mit identifierToken für Reconnect)
            debugStatusLabel.setText("Status: Connecting...");
            debugStatusLabel.setStyle("-fx-text-fill: orange;");

            gameServerService.connectToServer(mainApp, server, username, identifierToken)
                    .thenAccept(session -> {
                        Platform.runLater(() -> {
                            System.out.println(" DEBUG: Reconnect erfolgreich!");
                            System.out.println("   Session ID: " + session.getId());
                            System.out.println("   Session open: " + session.isOpen());
                            debugStatusLabel.setText("Status: Connected");
                            debugStatusLabel.setStyle("-fx-text-fill: green;");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            System.err.println(" DEBUG: Reconnect fehlgeschlagen: " + ex.getMessage());
                            ex.printStackTrace();
                            debugStatusLabel.setText("Status: Failed");
                            debugStatusLabel.setStyle("-fx-text-fill: red;");
                        });
                        return null;
                    });

        } catch (Exception e) {
            System.err.println(" DEBUG: Fehler beim Reconnect: " + e.getMessage());
            e.printStackTrace();
            debugStatusLabel.setText("Status: Error");
            debugStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    */

    private Circle getCircleFromButton(VBox button) {
        for (Node n : button.getChildren()) {
            if (n instanceof Circle c) return c;
            if (n instanceof StackPane sp) {
                for (Node child : sp.getChildren()) {
                    if (child instanceof Circle c) return c;
                }
            }
        }
        return null;
    }

    private Label getLabelFromButton(VBox button) {
        for (Node n : button.getChildren()) {
            if (n instanceof Label l) return l;
        }
        return null;
    }

    private void initMessagePane() {
        messagePane.setVisible(false);
        messagePane.setManaged(false);

        //Gesamtbreite = Spielfeldbreite
        messagePane.prefWidthProperty().bind(gameBoardView.widthProperty());
        messagePane.maxWidthProperty().bind(gameBoardView.widthProperty());

        messagePane.setAlignment(Pos.CENTER_LEFT);
        messagePane.setPadding(new Insets(8, 12, 8, 12));
        messagePane.setSpacing(10);

        //transparentes Design
        messagePane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);" + "-fx-background-radius: 10;");

        //Icons setzen (bitte Image-Pfad anpassen)
        leftMsgIcon.setImage(new Image(getClass().getResourceAsStream("/images/rufezeichen.png")));
        rightMsgIcon.setImage(new Image(getClass().getResourceAsStream("/images/rufezeichen.png")));

        leftMsgIcon.setFitWidth(60);
        leftMsgIcon.setFitHeight(60);
        leftMsgIcon.setPreserveRatio(true);

        rightMsgIcon.setFitWidth(60);
        rightMsgIcon.setFitHeight(60);
        rightMsgIcon.setPreserveRatio(true);

        //Text
        messageLabel.setFont(Font.font("Press Start 2P", 12));
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);

        //Label soll NICHT alles ziehen, sondern die Spacer machen das Zentrieren
        messageLabel.setMaxWidth(500); // optional, sonst kann er zu lang werden

        //Spacer wachsen lassen → Icons bleiben außen
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        leftSpacer.setMinWidth(0);
        rightSpacer.setMinWidth(0);

        messagePane.getChildren().setAll(leftMsgIcon, leftSpacer, messageLabel, rightSpacer, rightMsgIcon);
    }

    public void showError(String msg) {
        showMessage(msg, true);
    }

    public void showInfo(String msg) {
        showMessage(msg, false);
    }

    private void showMessage(String msg, boolean isError) {
        Platform.runLater(() -> {

            if (isError) {
                messagePane.setStyle("-fx-background-color: rgba(220, 50, 50, 0.55);" + "-fx-background-radius: 10;" + "-fx-padding: 8 12 8 12;");

                //ERROR PNG
                leftMsgIcon.setImage(new Image(getClass().getResourceAsStream("/images/blitz.png")));
                rightMsgIcon.setImage(new Image(getClass().getResourceAsStream("/images/blitz.png")));

            } else {
                messagePane.setStyle("-fx-background-color: rgba(7,36,149,0.55);" + "-fx-background-radius: 10;" + "-fx-padding: 8 12 8 12;");

                //INFO PNG
                leftMsgIcon.setImage(new Image(getClass().getResourceAsStream("/images/rufezeichen.png")));
                rightMsgIcon.setImage(new Image(getClass().getResourceAsStream("/images/rufezeichen.png")));
            }

            messageLabel.setText(msg);
            messagePane.setVisible(true);
            messagePane.setManaged(true);

            if (messageTimer != null) messageTimer.stop();

            messageTimer = new PauseTransition(Duration.seconds(3));
            messageTimer.setOnFinished(e -> {
                messagePane.setVisible(false);
                messagePane.setManaged(false);
            });
            messageTimer.playFromStart();
        });
    }

    /**
     * PUBLIC Methode: Wird von Main.java bei generellen Fehlern aufgerufen
     * Anzeigen der Fehler am Spielfeld
     */
    public void handleGeneralError(String message) {
        Platform.runLater(() -> {
            Stage stage = (Stage) Stage.getWindows().get(0);
            showInfo("Info vom Server: \n" + message);
        });
    }

    public Pane getRoot() {
        return root;
    }

    /**
     * Deaktiviert die Auto-KI (z.B. beim Spielende)
     */
    public void disableAutoAI() {
        // Setze Game Over Flag um weitere AI-Züge zu verhindern - MUSS ZUERST passieren!
        isGameOver = true;
        System.out.println(" Game Over Flag gesetzt - keine AI-Züge mehr möglich");

        // KRITISCH: Deaktiviere KI auch am Server, um zu verhindern dass Server noch Züge erwartet
        if (gameServerService != null && gameServerService.getCurrentSession() != null && gameServerService.getCurrentSession().isOpen()) {
            System.out.println(" Sende TOGGLE_AI(false) an Server...");
            gameServerService.sendToggleAI(false);
            System.out.println(" TOGGLE_AI(false) gesendet");
        }

        // SOFORT alle AI-Flags zurücksetzen (nicht erst in Platform.runLater)
        aiAutoMode = false;
        aiTurnInProgress = false;
        aiWaitingForMove = false;
        aiWaitingForSecondPush = false;
        currentAIDecision = null;
        System.out.println(" Alle AI-Flags zurückgesetzt");

        // Deaktiviere auch AFK-Modus
        if (afkModeActive) {
            deactivateAFKMode();
        }

        // Stoppe Timer
        if (currentTurnTimer != null && !currentTurnTimer.isDone()) {
            currentTurnTimer.cancel(false);
        }

        // Cleanup Executors - WICHTIG: shutdownNow() sofort aufrufen
        if (aiExecutor != null && !aiExecutor.isShutdown()) {
            System.out.println(" Stoppe AI Executor...");
            aiExecutor.shutdownNow();
        }
        if (turnTimerExecutor != null && !turnTimerExecutor.isShutdown()) {
            System.out.println(" Stoppe Turn Timer Executor...");
            turnTimerExecutor.shutdownNow();
        }

        // UI Update (kann asynchron passieren)
        if (aiAutoButton != null && aiAutoButton.isSelected()) {
            Platform.runLater(() -> {
                aiAutoButton.setSelected(false);
                System.out.println(" Auto-KI Button deaktiviert (Spielende)");
            });
        }
    }

    /**
     * Aktiviert den AFK-Modus: Auto-KI wird eingeschaltet, "Ich bin zurück" Button erscheint
     */
    private void activateAFKMode() {
        // Spiel ist beendet - kein AFK-Modus
        if (isGameOver) {
            System.out.println("   (Spiel beendet - kein AFK-Modus)");
            return;
        }

        if (afkModeActive) {
            System.out.println("   (AFK bereits aktiv - überspringe)");
            return; // Bereits im AFK-Modus
        }

        // Wenn Auto-KI bereits manuell aktiviert ist, nicht als AFK markieren
        if (aiAutoMode && aiAutoButton != null && aiAutoButton.isSelected()) {
            System.out.println("   (Auto-KI bereits manuell aktiv - kein AFK-Modus nötig)");
            return;
        }

        System.out.println(" TIMEOUT! Spieler ist AFK - aktiviere Auto-KI");
        afkModeActive = true;

        Platform.runLater(() -> {
            // Aktiviere Auto-KI
            if (aiAutoButton != null && !aiAutoButton.isSelected()) {
                aiAutoButton.setSelected(true);
                aiAutoMode = true;
            }

            // Zeige "Ich bin zurück" Button
            if (imBackButton != null) {
                imBackButton.setVisible(true);
                imBackButton.setManaged(true);
            }

            System.out.println(" AFK-Modus aktiviert - Auto-KI übernimmt");
        });
    }

    /**
     * Deaktiviert den AFK-Modus: Auto-KI wird ausgeschaltet, Button verschwindet
     */
    private void deactivateAFKMode() {
        if (!afkModeActive) {
            return; // Nicht im AFK-Modus
        }

        afkModeActive = false;

        Platform.runLater(() -> {
            // Deaktiviere Auto-KI
            // WICHTIG: Setze aiAutoMode ZUERST bevor wir den Button ändern
            // um Loop mit Listener zu vermeiden
            aiAutoMode = false;

            if (aiAutoButton != null && aiAutoButton.isSelected()) {
                aiAutoButton.setSelected(false);
            }

            // Verstecke "Ich bin zurück" Button
            if (imBackButton != null) {
                imBackButton.setVisible(false);
                imBackButton.setManaged(false);
            }

            System.out.println(" Willkommen zurück! AFK-Modus deaktiviert");
        });
    }

    /**
     * Wird von Main.java aufgerufen, wenn ein Turn Update vom Server kommt.
     * startet den Timer für den eigenen Zug
     * <p>
     * WICHTIG: Prüft auch ob Spieler durch Server-Timeout übersprungen wurde!
     */
    public void onTurnUpdate(String currentPlayerId, String turnEndTime) {
        // Prüfe ob es mein Zug ist
        boolean isMyTurn = gameState.getMyPlayerState() != null && gameState.getMyPlayerState().getPlayerInfo() != null && currentPlayerId != null && currentPlayerId.equals(gameState.getMyPlayerState().getPlayerInfo().getId());

        // KRITISCHE PRÜFUNG: War es vorher mein Zug und jetzt nicht mehr?
        if (wasMyTurnBefore && !isMyTurn && !afkModeActive) {
            // Zug ist vorbei - prüfe ob Timeout
            long turnDuration = System.currentTimeMillis() - turnStartTime;
            long timeSinceLastAction = System.currentTimeMillis() - lastPlayerActionTime;

            System.out.println("\n === ZUGWECHSEL ANALYSE ===");
            System.out.println("   Zug-Dauer: " + (turnDuration / 1000) + " Sekunden");
            System.out.println("   Zeit seit letzter Aktion: " + (timeSinceLastAction / 1000) + " Sekunden");

            // Wenn der Zug mindestens 15 Sekunden dauerte UND keine Aktion gemacht wurde
            // → Wahrscheinlich Server-Timeout!
            if (turnDuration > 15000 && timeSinceLastAction > 15000) {
                System.out.println("    SERVER-TIMEOUT ERKANNT!");
                System.out.println("   → Spieler hat " + (turnDuration / 1000) + "s NICHT gespielt");
                System.out.println("   → Aktiviere AFK-Modus für nächsten Zug!\n");

                // Aktiviere AFK-Modus
                activateAFKMode();
            } else {
                System.out.println("    Spieler war aktiv - kein AFK\n");
            }
        }

        if (isMyTurn) {
            // Mein Zug - starte Timer
            System.out.println("\n === MEIN ZUG ===");
            wasMyTurnBefore = true;
            turnStartTime = System.currentTimeMillis();
            startTurnTimer(turnEndTime);
        } else {
            // Nicht mein Zug - stoppe Timer falls läuft
            if (currentTurnTimer != null && !currentTurnTimer.isDone()) {
                currentTurnTimer.cancel(false);
                System.out.println(" Timer gestoppt - nicht mein Zug");
            }
            wasMyTurnBefore = false;
        }
    }

    /**
     * Wird aufgerufen wenn der Spieler eine Aktion macht
     * Zeigt dass der Spieler aktiv ist (nicht AFK)
     * WICHTIG: Muss bei JEDEM manuellen Command aufgerufen werden!
     */
    public void onPlayerAction() {
        // Timestamp der letzten Aktion aktualisieren
        lastPlayerActionTime = System.currentTimeMillis();
        System.out.println(" Spieler-Aktion registriert");

        // Wenn Spieler eine Aktion macht, ist er definitiv nicht AFK
        if (afkModeActive) {
            System.out.println(" Spieler hat Aktion gemacht - deaktiviere AFK-Modus");
            deactivateAFKMode();
        }
    }

    /**
     * Startet den Turn Timer basierend auf turnEndTime
     * Bei Timeout wird automatisch der AFK-Modus aktiviert
     * <p>
     * DYNAMISCH: Passt sich automatisch an Server-Timeout an (20s, 60s, etc.)
     */
    private void startTurnTimer(String turnEndTime) {
        // Stoppe vorherigen Timer falls vorhanden
        if (currentTurnTimer != null && !currentTurnTimer.isDone()) {
            currentTurnTimer.cancel(false);
        }

        Long timeoutSeconds = null;
        Long serverTimeSeconds = null;

        if (turnEndTime != null && !turnEndTime.isEmpty()) {
            try {
                // Parse turnEndTime (ISO 8601 format: "2026-01-20T14:30:00" oder mit Z/Timezone)
                java.time.LocalDateTime endTime;
                endTime = LocalDateTime.ofInstant(TimeParsing.parseToInstantUtc(turnEndTime), ZoneId.systemDefault());
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                long secondsUntilTimeout = java.time.Duration.between(now, endTime).getSeconds();
                serverTimeSeconds = secondsUntilTimeout;

                System.out.println(" Turn Timer Info (DYNAMISCH):");
                System.out.println("   Server turnEndTime: " + turnEndTime);
                System.out.println("   Client Zeit: " + now);
                System.out.println("   Server Zeit bis Timeout: " + secondsUntilTimeout + " Sekunden");

                // KRITISCH: Wenn Zeit bereits abgelaufen oder sehr kurz
                // Aber nur wenn Spieler schon mindestens eine Aktion gemacht hat!
                if (secondsUntilTimeout <= 2) {
                    System.out.println(" Turn bereits abgelaufen oder < 2 Sekunden!");

                    // Prüfe ob Spieler schon aktiv war (hat schon Aktionen gemacht)
                    boolean playerWasActive = lastPlayerActionTime > 0;

                    if (playerWasActive) {
                        System.out.println("        → SOFORTIGE AFK-Aktivierung (Spieler war schon aktiv)!");
                        Platform.runLater(() -> activateAFKMode());
                    } else {
                        System.out.println("        → KEINE AFK-Aktivierung (Spieler war noch nicht aktiv - wahrscheinlich Spielstart/Serverswitch)");
                        // Setze einen Minimum Timer von 5 Sekunden
                        timeoutSeconds = 5L;
                    }
                    return;
                }

                // DYNAMISCHER PUFFER: 15% der Server-Zeit oder min. 2 Sekunden
                long bufferSeconds = Math.max(2, (long) (secondsUntilTimeout * 0.15));
                timeoutSeconds = Math.max(5L, secondsUntilTimeout - bufferSeconds);

                System.out.println("   → Sicherheitspuffer: " + bufferSeconds + "s (15% von " + secondsUntilTimeout + "s)");
                System.out.println("   → Client Timer: " + timeoutSeconds + " Sekunden");
                System.out.println("    Dynamische Anpassung an Server-Timer!");

            } catch (Exception e) {
                System.err.println(" Fehler beim Parsen von turnEndTime: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Fallback wenn keine gültige Zeit vom Server kam
        if (timeoutSeconds == null) {
            // Standard: 50 Sekunden (Annahme: Server verwendet 60s)
            timeoutSeconds = 50L;
            System.out.println(" Keine turnEndTime vom Server");
            System.out.println("   → Verwende Fallback: " + timeoutSeconds + " Sekunden");
        }

        // Starte Timer mit berechneter Zeit
        final long finalTimeout = timeoutSeconds;
        final Long finalServerTime = serverTimeSeconds;

        currentTurnTimer = turnTimerExecutor.schedule(() -> {
            System.out.println("\n TIMEOUT REACHED! ");
            System.out.println("        Client Timer: " + finalTimeout + " Sekunden");
            if (finalServerTime != null) {
                System.out.println("        Server Timer war: " + finalServerTime + " Sekunden");
            }
            System.out.println("        → Aktiviere AFK-Modus!\n");
            activateAFKMode();
        }, timeoutSeconds, TimeUnit.SECONDS);

        System.out.println(" AFK-Timer gestartet: " + timeoutSeconds + " Sekunden");
        if (serverTimeSeconds != null) {
            System.out.println("  (reagiert " + (serverTimeSeconds - timeoutSeconds) + "s VOR Server-Timeout)\n");
        } else {
            System.out.println();
        }
    }

    private void setActiveBonusType(BonusType bonusType) {
        this.activeBonusType = bonusType;
    }

    private boolean toggleActiveBonusType(BonusType bonusType) {
        if (this.activeBonusType == bonusType) {
            this.activeBonusType = null;
            return false;
        } else {
            activeBonusType = bonusType;
            return true;
        }
    }

    private void updateFromGameState(GameState gs, GameServerService gameServerService) {
        this.activeBonusType = null;

        // --- Board und Pawns aktualisieren ---
        if (gs.getBoard() != null && gameBoardView != null) {
            gameBoardView.setTiles(gs.getBoard());
            gameBoardView.drawTiles();
            gameBoardView.drawAllPawns(gs.getPlayers());
        }

        if (gs.getMyPlayerState() != null) {
            gameBoardView.setHomePosition(gs.getMyPlayerState().getHomePosition());
            gameBoardView.setRemainingTreasureCount(gs.getMyPlayerState().getRemainingTreasureCount());
        }

        if (gameState.getExtraTile() != null && extraTile != null) {
            extraTile.getChildren().setAll(gameBoardView.createTileImage(gameState.getExtraTile(), tileSize));
        }

        // Gefundene Schätze Anzahl aktualisieren
        treasuresFound = gs.getMyPlayerState().getTreasuresFound().size();
        // SchatzInfo-Label aktualisieren
        treasuresFoundAndTotal.setText(treasuresFound + " / " + totalTreasures);

        updateButtons(gameState.getMyPlayerState().getAvailableBonuses(), gameServerService);

        // Spielerliste aktualisieren
        updatePlayerList(gs.getPlayers());

        // Aktuell gesuchten Schatz aktualisieren
        if (currentTreasure != null) {
            if (gs.getMyPlayerState().getCurrentTreasure() != null) {
                int id = gs.getMyPlayerState().getCurrentTreasure().getId();
                currentTreasure.setImage(new Image(getClass().getResourceAsStream("/images/treasure" + id + ".png")));
            } else {
                currentTreasure.setImage(new Image(getClass().getResourceAsStream("/images/treasure.png")));
            }
        }

        if (gs.getMyPlayerState().getRemainingTreasureCount() == 0) {
            currentTreasure.setImage(null);
        }

        // Anzeige der verbleibenden Schatzkarten aktualisieren
        if (gs.getMyPlayerState().getRemainingTreasureCount() < 6) {
            remainingTreasures.setImage(new Image(getClass().getResourceAsStream("/images/remaining_treasures" + gs.getMyPlayerState().getRemainingTreasureCount() + ".png")));
        }

        // Prüfe ob ich am Zug bin
        boolean isMyTurn = gs.getCurrentPlayerId() != null && gs.getMyPlayerState() != null && gs.getMyPlayerState().getPlayerInfo() != null && gs.getCurrentPlayerId().equals(gs.getMyPlayerState().getPlayerInfo().getId());

        // Push-Arrows ausblenden wenn Spieler nicht am Zug
        if (!isMyTurn) {
            gameBoardView.setPushButtonMode(GameBoardView.PushButtonMode.NONE);
            myTurnLabel.setText("");
            pawnColor.setVisible(false);
            rotateButton.setVisible(false);

            // WICHTIG: Wenn ich nicht am Zug bin, resette lastProcessedTurnId
            // Damit der nächste Zug neu getriggert werden kann
            if (lastProcessedTurnId != null && !isMyTurn) {
                System.out.println(" Anderer Spieler am Zug - Reset TurnID");
                lastProcessedTurnId = null;
            }
            // Push-Arrows einblenden wenn Spieler am Zug und WAITING_FOR_PUSH
        } else if (gs.getCurrentTurnState() == TurnState.WAITING_FOR_PUSH) {
            rotateButton.setVisible(true);
            gameBoardView.setPushButtonMode(GameBoardView.PushButtonMode.NORMAL);
            if (activeBonusType == PUSH_FIXED) {
                gameBoardView.setPushButtonMode(ALL);
                gameBoardView.setActiveBonus(PUSH_FIXED);
            }

            // AUTO-KI: Wenn aktiviert und ich am Zug bin, führe automatisch Zug aus
            String currentTurnId = gs.getCurrentPlayerId() + "_" + gs.getCurrentTurnState();

            if (aiAutoMode && aiService != null && !aiTurnInProgress && !aiWaitingForMove && !aiWaitingForSecondPush && !isGameOver) {
                // Prüfe ob dieser Zug schon verarbeitet wurde
                if (lastProcessedTurnId == null || !lastProcessedTurnId.equals(currentTurnId)) {
                    lastProcessedTurnId = currentTurnId;
                    aiRetryCount = 0; // Reset Retry-Counter für neuen Zug
                    System.out.println(" Auto-KI: Führe automatischen Zug aus... (TurnID: " + currentTurnId + ")");
                    executeAITurn(gameServerService);
                } else {
                    System.out.println("  Überspringe: Zug wurde bereits verarbeitet");
                }
            }

            // PUSH_TWICE: Zweiter Push!
            if (aiWaitingForSecondPush && gs.getCurrentTurnState() == TurnState.WAITING_FOR_PUSH && !isGameOver) {
                System.out.println("\n === PUSH_TWICE: ZWEITER PUSH ===");
                System.out.println("   Server ist in WAITING_FOR_PUSH");
                System.out.println("   aiWaitingForSecondPush: " + aiWaitingForSecondPush);
                System.out.println("   currentAIDecision: " + (currentAIDecision != null ? "vorhanden" : "NULL"));

                aiWaitingForSecondPush = false;
                System.out.println("    aiWaitingForSecondPush = FALSE gesetzt");

                if (currentAIDecision != null) {
                    System.out.println("   Führe zweiten Push aus:");
                    System.out.println("     Index: " + currentAIDecision.getPushRowOrCol());
                    System.out.println("     Direction: " + currentAIDecision.getPushDirection());

                    aiExecutor.schedule(() -> {
                        // KRITISCH: Prüfe sofort ob Thread interrupted oder Spiel beendet
                        if (Thread.currentThread().isInterrupted() || isGameOver) {
                            System.out.println(" PUSH_TWICE zweiter Push abgebrochen - Spiel beendet");
                            return;
                        }

                        System.out.println("\n Sende zweiten Push...");
                        Platform.runLater(() -> {
                            if (isGameOver) {
                                System.out.println(" Zweiter Push abgebrochen - Spiel beendet");
                                return;
                            }

                            gameServerService.sendPushTile(currentAIDecision.getPushRowOrCol(), currentAIDecision.getPushDirection());
                            System.out.println(" Zweiter Push gesendet");

                            // Jetzt warten wir auf WAITING_FOR_MOVE
                            aiWaitingForMove = true;
                            System.out.println(" aiWaitingForMove = TRUE gesetzt");
                            System.out.println(" Warte auf WAITING_FOR_MOVE vom Server...");
                            System.out.println("====================================\n");
                        });
                    }, 500, TimeUnit.MILLISECONDS);
                } else {
                    System.out.println("    FEHLER: currentAIDecision ist NULL!");
                    System.out.println("====================================\n");
                }
            }
        } else if (gs.getCurrentTurnState() == TurnState.WAITING_FOR_MOVE) {
            gameBoardView.setPushButtonMode(GameBoardView.PushButtonMode.NONE);
            rotateButton.setVisible(false);

            // Prüfe ob aktueller Spieler AI-gesteuert ist
            PlayerState currentPlayer = gs.getPlayerById(gs.getCurrentPlayerId());
            boolean isCurrentPlayerAI = currentPlayer != null && currentPlayer.getPlayerInfo() != null && currentPlayer.getPlayerInfo().getIsAiControlled();

            if (isCurrentPlayerAI) {
                myTurnLabel.setText("🤖 Spieler bewegt sich (KI)");
            } else {
                myTurnLabel.setText("Spielfigur bewegen");
            }

            myTurnLabel.setFont(Font.font("Press Start 2P", 12));
            myTurnLabel.setTextAlignment(TextAlignment.CENTER);
            myTurnLabel.setAlignment(Pos.CENTER);

            pawnColor.setStroke(Color.WHITE);
            pawnColor.setStrokeWidth(1);
            pawnColor.setFill(Color.web(gs.getMyPlayerState().getPlayerInfo().getColor()));
            pawnColor.setVisible(true);

            // KRITISCH: Reset aiWaitingForMove wenn nicht mehr mein Zug
            if (!isMyTurn && aiWaitingForMove) {
                aiWaitingForMove = false;
            }

            // MOVE PHASE: Prüfe ob AI Move ausführen soll
            // WICHTIG: Nur wenn isMyTurn == true!
            boolean shouldExecuteMove = isMyTurn && !isGameOver && (aiWaitingForMove || (aiAutoMode && !aiTurnInProgress && !aiWaitingForMove));

            if (shouldExecuteMove) {
                executeAIMove(gameServerService);
            }
        }
        LapTimer.lap("GameScreen updateFromGameState");
    }

    /**
     * PHASE 2: Führt die Bewegung aus (NACH dem Push, mit aktuellem Board!)
     * Prüft auch BEAM und SWAP Boni (nur in MOVE-Phase erlaubt!)
     */
    private void executeAIMove(GameServerService gameServerService) {
        // Prüfe ob Spiel vorbei ist
        if (isGameOver) {
            System.out.println(" executeAIMove() abgebrochen - Spiel ist beendet");
            return;
        }

        System.out.println("\n === executeAIMove() GESTARTET ===");
        System.out.println("   VORHER:");
        System.out.println("     aiWaitingForMove = " + aiWaitingForMove);
        System.out.println("     aiTurnInProgress = " + aiTurnInProgress);

        // Reset Flags - Move ist letzter Schritt
        aiWaitingForMove = false;
        System.out.println("   NACHHER:");
        System.out.println("     aiWaitingForMove = FALSE");
        System.out.println("   (aiTurnInProgress bleibt TRUE bis resetAI())");

        try {
            SafeMoveStrategy strategy = new SafeMoveStrategy();

            // Normaler Move: Berechne erreichbare Felder
            // WICHTIG: Alle Boni wurden bereits in der Push-Phase ausgewertet!
            System.out.println("   Berechne sichere Bewegungen...");
            List<SafeMoveStrategy.SafeMoveOption> safeMoves = strategy.findSafeMoves(gameState);

            if (safeMoves.isEmpty()) {
                System.err.println("Keine sicheren Züge gefunden!");
                resetAI();
                return;
            }

            // Wähle bestes Ziel (nächstes zum Schatz oder Home)
            SafeMoveStrategy.SafeMoveOption bestMove = safeMoves.get(0);

            // DEBUG: Detaillierte Ausgabe
            String targetType = bestMove.isGoingHome ? "HOME" : "SCHATZ";
            System.out.println("\n=== AI BEWEGUNG ===");
            System.out.println("Spieler: " + gameState.getMyPlayerState().getPlayerInfo().getName());
            System.out.println("Aktuelle Position: (" + gameState.getMyPlayerState().getCurrentPosition().getColumn() + "," + gameState.getMyPlayerState().getCurrentPosition().getRow() + ")");
            System.out.println("Heimatposition: (" + gameState.getMyPlayerState().getHomePosition().getColumn() + "," + gameState.getMyPlayerState().getHomePosition().getRow() + ")");
            System.out.println("Verbleibende Schätze: " + gameState.getMyPlayerState().getRemainingTreasureCount());
            System.out.println("Gehe zu: (" + bestMove.targetPosition.column + "," + bestMove.targetPosition.row + ")");
            System.out.println("Ziel-Typ: " + targetType);
            System.out.println("Distanz zum Ziel: " + bestMove.distanceToTarget);
            System.out.println("===================\n");

            // Sende Bewegung
            System.out.println("\n Sende Move Command...");

            if (isGameOver) {
                System.out.println(" Move Command abgebrochen - Spiel beendet");
                resetAI();
                return;
            }

            gameServerService.sendMovePlayer(bestMove.targetPosition);
            System.out.println(" Move Command gesendet");

            System.out.println(" AI-Zug komplett ausgeführt!");
            System.out.println("=====================================\n");

        } catch (Exception e) {
            System.err.println(" FEHLER bei AI-Bewegung: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println(" executeAIMove() ruft resetAI() auf...");
            resetAI();
        }
    }

    private void resetAI() {
        System.out.println("\n === resetAI() aufgerufen ===");
        System.out.println("   VORHER:");
        System.out.println("     aiTurnInProgress: " + aiTurnInProgress);
        System.out.println("     aiWaitingForMove: " + aiWaitingForMove);
        System.out.println("     aiWaitingForSecondPush: " + aiWaitingForSecondPush);
        System.out.println("     currentAIDecision: " + (currentAIDecision != null ? "set" : "null"));

        // Stack Trace um zu sehen, wer resetAI() aufgerufen hat
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        System.out.println("   Aufgerufen von:");
        if (stackTrace.length > 2) {
            System.out.println("     " + stackTrace[2].getClassName() + "." + stackTrace[2].getMethodName() + ":" + stackTrace[2].getLineNumber());
        }

        aiTurnInProgress = false;
        aiWaitingForMove = false;
        aiWaitingForSecondPush = false;
        currentAIDecision = null;
        // lastProcessedTurnId bleibt - wird beim nächsten WAITING_FOR_PUSH neu gesetzt
        if (aiButton != null) {
            aiButton.setText("KI-Zug");
            aiButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");
            // Button wieder aktivieren wenn Auto-KI nicht läuft
            if (!aiAutoMode) {
                aiButton.setDisable(false);
            }
        }

        System.out.println("   NACHHER: Alle Flags auf FALSE, Decision auf NULL");
        System.out.println(" AI zurückgesetzt - bereit für nächsten Zug");
        System.out.println("===============================\n");
    }

    /**
     * PUBLIC Methode: Wird von Main.java bei Push-Back Fehler aufgerufen
     * Versucht automatisch einen anderen Push
     */
    public void handlePushBackError() {
        System.out.println("\n === PUSH-BACK RETRY MECHANISMUS ===");
        System.out.println("Retry-Versuch: " + (aiRetryCount + 1) + " / " + MAX_AI_RETRIES);

        if (aiRetryCount >= MAX_AI_RETRIES) {
            System.out.println(" Max Retries erreicht - gebe auf");
            System.out.println("   Bitte manuell spielen oder Zug überspringen\n");
            resetAI();
            aiRetryCount = 0;

            // Zeige Warnung
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("AI Fehler");
                alert.setHeaderText("Push-Back Problem");
                alert.setContentText("Die AI konnte nach " + MAX_AI_RETRIES + " Versuchen keinen gültigen Push finden.\nBitte spiele manuell weiter.");
                alert.show();
            });
            return;
        }

        aiRetryCount++;
        aiTurnInProgress = false; // Reset damit neuer Versuch möglich ist
        aiWaitingForMove = false;

        System.out.println(" Versuche ANDEREN Push...\n");

        // Warte kurz und versuche erneut
        aiExecutor.schedule(() -> {
            executeAITurn(gameServerService);
        }, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Führt einen AI-gesteuerten Spielzug aus (einmalig)
     */
    private void executeAITurn(GameServerService gameServerService) {
        // Prüfe ob Spiel vorbei ist
        if (isGameOver) {
            System.out.println(" executeAITurn() abgebrochen - Spiel ist beendet");
            return;
        }

        System.out.println("\n === executeAITurn() AUFGERUFEN ===");
        System.out.println("   aiService: " + (aiService != null ? "vorhanden" : "NULL"));
        System.out.println("   aiTurnInProgress: " + aiTurnInProgress);

        if (aiService == null || aiTurnInProgress) {
            System.out.println("    Abbruch: " + (aiService == null ? "aiService ist NULL" : "aiTurnInProgress bereits TRUE"));
            System.out.println("=====================================\n");
            return;
        }

        aiTurnInProgress = true;
        System.out.println("    aiTurnInProgress = TRUE gesetzt");
        System.out.println("   Aktuelle Phase: " + gameState.getCurrentTurnState());
        System.out.println("   AI-Zug wird berechnet...");
        System.out.println("=====================================\n");

        // Button während Berechnung deaktivieren
        if (aiButton != null) {
            Platform.runLater(() -> {
                aiButton.setText("KI denkt...");
                aiButton.setDisable(true);
                aiButton.setStyle("-fx-background-color: #D3D3D3; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");
            });
        }

        aiExecutor.schedule(() -> {
            // KRITISCH: Prüfe sofort ob Thread interrupted oder Spiel beendet
            if (Thread.currentThread().isInterrupted() || isGameOver) {
                System.out.println(" AI-Berechnung abgebrochen - Spiel beendet oder Thread interrupted");
                return;
            }

            try {
                System.out.println("\n AI-Executor: Starte AI-Berechnung...");
                // Hole AI-Entscheidung
                AIDecision decision = aiService.getNextMove(gameState);
                System.out.println(" AI-Berechnung abgeschlossen");

                System.out.println("\n=== AI ENTSCHEIDUNG ===");
                System.out.println("Bonus: " + decision.getUseBonus());
                System.out.println("Geht nach Hause: " + decision.isGoingHome());
                System.out.println("Rotationen: " + decision.getRotations());
                System.out.println("Push: Index=" + decision.getPushRowOrCol() + ", Richtung=" + decision.getPushDirection());
                if (decision.getMoveTarget() != null) {
                    System.out.println("Move: " + decision.getMoveTarget().column + "," + decision.getMoveTarget().row);
                }
                System.out.println("Begründung: " + decision.getReasoning());
                System.out.println("=======================");

                // BONUS-HANDLING: Prüfe ob ein Bonus genutzt werden soll
                if (decision.getUseBonus() != null) {
                    System.out.println("\n === BONUS WIRD VERWENDET ===");
                    System.out.println("   Bonus-Typ: " + decision.getUseBonus());
                    System.out.println("   Aktuelle Phase: " + gameState.getCurrentTurnState());
                    System.out.println("   aiTurnInProgress: " + aiTurnInProgress);
                    System.out.println("   aiWaitingForMove: " + aiWaitingForMove);
                    System.out.println("   aiWaitingForSecondPush: " + aiWaitingForSecondPush);
                    System.out.println("================================\n");

                    switch (decision.getUseBonus()) {
                        case BEAM:
                            System.out.println(" AI führt BEAM aus zu: " + decision.getBeamTarget().column + "," + decision.getBeamTarget().row);
                            Platform.runLater(() -> {
                                if (isGameOver) {
                                    System.out.println(" BEAM Command abgebrochen - Spiel beendet");
                                    return;
                                }
                                gameServerService.sendBeamCommand(decision.getBeamTarget());
                                System.out.println(" BEAM Command gesendet - rufe resetAI() auf");
                                resetAI();
                            });
                            System.out.println(" BEAM: Return aus executeAITurn()");
                            return; // Zug beendet

                        case SWAP:
                            System.out.println(" AI führt SWAP aus mit Spieler: " + decision.getSwapTargetPlayerId());
                            Platform.runLater(() -> {
                                if (isGameOver) {
                                    System.out.println(" SWAP Command abgebrochen - Spiel beendet");
                                    return;
                                }
                                gameServerService.sendSwapCommand(decision.getSwapTargetPlayerId());
                                System.out.println(" SWAP Command gesendet - rufe resetAI() auf");
                                resetAI();
                            });
                            System.out.println(" SWAP: Return aus executeAITurn()");
                            return; // Zug beendet

                        case PUSH_TWICE:
                            System.out.println("\n === PUSH_TWICE AKTIVIERT ===");
                            System.out.println("   Schritt 1: Sende PUSH_TWICE Command");
                            System.out.println("   Schritt 2: Führe ersten Push aus");
                            System.out.println("   Schritt 3: Warte auf zweiten Push (nicht auf Move!)");
                            System.out.println("====================================\n");

                            Platform.runLater(() -> {
                                if (isGameOver) {
                                    System.out.println(" PUSH_TWICE Command abgebrochen - Spiel beendet");
                                    return;
                                }
                                System.out.println(" Sende PUSH_TWICE Command an Server...");
                                gameServerService.sendPushTwiceCommand();
                                System.out.println(" PUSH_TWICE Command gesendet");
                            });

                            // Speichere Decision für zweiten Push
                            currentAIDecision = decision;
                            aiWaitingForSecondPush = true;
                            System.out.println(" aiWaitingForSecondPush = true gesetzt");
                            System.out.println(" currentAIDecision gespeichert");

                            // Verzögere ersten Push um 500ms
                            System.out.println("  Plane ersten Push in 500ms...");
                            aiExecutor.schedule(() -> {
                                // KRITISCH: Prüfe sofort ob Thread interrupted oder Spiel beendet
                                if (Thread.currentThread().isInterrupted() || isGameOver) {
                                    System.out.println(" PUSH_TWICE erster Push abgebrochen - Spiel beendet");
                                    return;
                                }
                                System.out.println(" Führe ersten Push aus (PUSH_TWICE)...");
                                continueAfterBonus(decision);
                            }, 500, TimeUnit.MILLISECONDS);
                            System.out.println(" PUSH_TWICE: Return aus executeAITurn()");
                            return; // Fortsetzung erfolgt in continueAfterBonus()

                        case PUSH_FIXED:
                            System.out.println(" AI nutzt PUSH_FIXED - normaler Flow");
                            // PUSH_FIXED erweitert nur die Optionen, normaler Flow weiter unten
                            break;
                    }
                } else {
                    System.out.println("  Kein Bonus wird verwendet - normaler Push/Move Flow");
                }

                // Für PUSH_FIXED und no-bonus: Führe normalen Flow aus
                continueAfterBonus(decision);
            } catch (Exception e) {
                System.err.println("AI-Fehler: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    aiTurnInProgress = false;

                    // Button zurücksetzen
                    if (aiButton != null) {
                        aiButton.setText("KI-Zug");
                        aiButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-family: 'Super Mario 256'; -fx-font-size: 14px; -fx-padding: 10px 20px;");

                        // Button wieder aktivieren für erneuten Versuch (außer Auto-KI läuft)
                        if (!aiAutoMode) {
                            aiButton.setDisable(false);
                        }
                    }

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("AI Fehler");
                    alert.setHeaderText("Die KI konnte keinen Zug berechnen");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
            }
        }, 500, TimeUnit.MILLISECONDS); // 0.5 Sekunden Verzögerung
    }

    /**
     * Fortsetzung der AI-Ausführung nach PUSH_TWICE Bonus
     */
    private void continueAfterBonus(AIDecision decision) {
        System.out.println("\n === continueAfterBonus() aufgerufen ===");
        System.out.println("   Bonus: " + (decision.getUseBonus() != null ? decision.getUseBonus() : "none"));
        System.out.println("   aiWaitingForSecondPush: " + aiWaitingForSecondPush);
        System.out.println("   Push: Index=" + decision.getPushRowOrCol() + ", Dir=" + decision.getPushDirection());
        System.out.println("   Rotations: " + decision.getRotations());

        // KRITISCHE VALIDIERUNG: Prüfe Push-Back!
        if (decision.getPushDirection() != null && !validatePushIsNotPushBack(decision.getPushRowOrCol(), decision.getPushDirection())) {
            System.out.println(" KRITISCHER FEHLER: AI wählte Push-Back! Korrigiere...");
            // Suche alternative Push-Aktion
            SafeMoveStrategy.PushInfo safePush = new SafeMoveStrategy().findSafePush(gameState);
            decision.setPushRowOrCol(safePush.index);
            decision.setPushDirection(safePush.direction);
            System.out.println(" Korrigiert zu: Index=" + safePush.index + ", Dir=" + safePush.direction);
        } else {
            System.out.println(" Push ist valide (kein Push-Back)");
        }

        // Sicherheits-Check: Validiere, dass die Bewegung zur aktuellen Position ist oder leer
        Coordinates currentPos = gameState.getMyPlayerState().getCurrentPosition();
        if (decision.getMoveTarget() == null || decision.getMoveTarget().column < 0 || decision.getMoveTarget().row < 0) {
            System.out.println("  WARNUNG: Ungültige Koordinaten, verwende aktuelle Position");
            decision.setMoveTarget(new Coordinates(currentPos.getColumn(), currentPos.getRow()));
        } else {
            System.out.println(" MoveTarget valide: (" + decision.getMoveTarget().column + "," + decision.getMoveTarget().row + ")");
        }

        // Führe Rotationen und Push aus
        System.out.println("  Starte executeRotationsAndPush()...");
        executeRotationsAndPush(decision, 0);
    }

    /**
     * Führt Rotationen und Push sequenziell aus mit korrekten Delays
     * WICHTIG: Verwendet aiExecutor statt JavaFX Thread um Blockierung zu vermeiden!
     */
    private void executeRotationsAndPush(AIDecision decision, int currentRotation) {
        System.out.println("\n executeRotationsAndPush() - Rotation " + currentRotation + "/" + decision.getRotations());

        if (currentRotation < decision.getRotations()) {
            // Nächste Rotation ausführen
            System.out.println(" Sende Rotation " + (currentRotation + 1) + "...");
            Platform.runLater(() -> {
                if (isGameOver) {
                    System.out.println(" Rotate Command abgebrochen - Spiel beendet");
                    return;
                }
                gameServerService.sendRotateCommand();
            });

            // Plane nächste Rotation nach 400ms
            aiExecutor.schedule(() -> {
                // KRITISCH: Prüfe sofort ob Thread interrupted oder Spiel beendet
                if (Thread.currentThread().isInterrupted() || isGameOver) {
                    System.out.println(" Rotation abgebrochen - Spiel beendet");
                    return;
                }
                executeRotationsAndPush(decision, currentRotation + 1);
            }, 400, TimeUnit.MILLISECONDS);

        } else {
            // Alle Rotationen fertig - plane Push
            long pushDelay = (decision.getRotations() > 0) ? 500 : 0;
            System.out.println(" Alle Rotationen fertig, plane Push in " + pushDelay + "ms...");

            aiExecutor.schedule(() -> {
                // KRITISCH: Prüfe sofort ob Thread interrupted oder Spiel beendet
                if (Thread.currentThread().isInterrupted() || isGameOver) {
                    System.out.println(" Push abgebrochen - Spiel beendet");
                    return;
                }

                System.out.println("\n === FÜHRE PUSH AUS ===");
                System.out.println("   Push: Index=" + decision.getPushRowOrCol() + ", Dir=" + decision.getPushDirection());
                System.out.println("   aiWaitingForSecondPush: " + aiWaitingForSecondPush);

                Platform.runLater(() -> {
                    if (isGameOver) {
                        System.out.println(" PushTile Command abgebrochen - Spiel beendet");
                        return;
                    }

                    // Schiebe Tile ein
                    gameServerService.sendPushTile(decision.getPushRowOrCol(), decision.getPushDirection());
                    System.out.println(" sendPushTile() aufgerufen");

                    // WICHTIG: Bei PUSH_TWICE warten wir auf zweiten Push, nicht auf Move!
                    if (aiWaitingForSecondPush) {
                        System.out.println("\n PUSH_TWICE: Erster Push ausgeführt");
                        System.out.println("    Warte auf WAITING_FOR_PUSH für zweiten Push...");
                        System.out.println("   aiWaitingForSecondPush bleibt TRUE");
                        System.out.println("   aiWaitingForMove = FALSE");
                        // aiWaitingForSecondPush bleibt true
                    } else {
                        // Normaler Push - warte auf Move-Phase
                        aiWaitingForMove = true;
                        System.out.println("\n Normaler Push ausgeführt");
                        System.out.println("    Warte auf WAITING_FOR_MOVE vom Server...");
                        System.out.println("   aiWaitingForMove = TRUE");
                    }

                    // Erfolgreicher Push → Reset Retry Counter
                    aiRetryCount = 0;

                    System.out.println("========================\n");
                    // PHASE 2 passiert in updateFromGameState() wenn WAITING_FOR_MOVE kommt!
                });
            }, pushDelay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * FINALE VALIDIERUNG: Prüft ob ein Push ein Push-Back ist
     * Diese Methode ist die LETZTE Verteidigungslinie vor dem Senden!
     */
    private boolean validatePushIsNotPushBack(int index, Direction direction) {
        LastPush lastPush = gameState.getLastPush();

        if (lastPush == null) {
            System.out.println(" Keine lastPush - Push ist OK");
            return true; // Kein lastPush → alles OK
        }

        int lastIndex = lastPush.getRowOrColumnIndex();
        Direction lastDirection = lastPush.getDirection();

        System.out.println("\n  === FINALE PUSH-BACK VALIDIERUNG ===");
        System.out.println("LastPush: Index=" + lastIndex + ", Direction=" + lastDirection);
        System.out.println("Geplanter Push: Index=" + index + ", Direction=" + direction);

        if (lastIndex != index) {
            System.out.println(" Anderer Index (" + lastIndex + " != " + index + ") - Push ist OK\n");
            return true; // Anderer Index → OK
        }

        System.out.println("  Gleicher Index! Prüfe Richtung...");

        // Prüfe ob entgegengesetzt (PUSH-BACK!)
        boolean isPushBack = (lastDirection == Direction.UP && direction == Direction.DOWN) || (lastDirection == Direction.DOWN && direction == Direction.UP) || (lastDirection == Direction.LEFT && direction == Direction.RIGHT) || (lastDirection == Direction.RIGHT && direction == Direction.LEFT);

        if (isPushBack) {
            System.out.println(" PUSH-BACK ERKANNT! Dieser Push ist VERBOTEN!\n");
        } else {
            System.out.println(" Gleiche Richtung oder nicht entgegengesetzt - Push ist OK\n");
        }

        return !isPushBack; // true = OK, false = PUSH-BACK
    }

    /**
     * Aktualisiert die Spieler-Liste mit AI- und Verbindungs-Indikatoren
     * + Mein Spieler steht immer ganz oben
     */
    private void updatePlayerList(List<PlayerState> players) {
        Platform.runLater(() -> {
            if (playerListBox == null || players == null) {
                return;
            }

            playerListBox.getChildren().clear();

            String currentPlayerId = gameState != null ? gameState.getCurrentPlayerId() : null;

            String myName = gameState != null && gameState.getMyPlayerState() != null && gameState.getMyPlayerState().getPlayerInfo() != null ? gameState.getMyPlayerState().getPlayerInfo().getName() : null;

            // Kopie sortieren: Ich zuerst
            List<PlayerState> sortedPlayers = new ArrayList<>(players);
            if (myName != null) {
                sortedPlayers.sort((p1, p2) -> {
                    if (p1 == null || p2 == null) return 0;
                    if (p1.getPlayerInfo() == null || p2.getPlayerInfo() == null) return 0;

                    boolean isMe1 = myName.equals(p1.getPlayerInfo().getName());
                    boolean isMe2 = myName.equals(p2.getPlayerInfo().getName());
                    return Boolean.compare(!isMe1, !isMe2);
                });
            }

            int shown = 0;

            // Echte Spieler anzeigen
            for (PlayerState player : sortedPlayers) {
                if (player == null || player.getPlayerInfo() == null) continue;

                PlayerInfo info = player.getPlayerInfo();
                boolean isCurrentPlayer = currentPlayerId != null && currentPlayerId.equals(info.getId());

                Circle colorCircle = new Circle(8);
                colorCircle.setFill(Color.web(info.getColor()));
                colorCircle.setStroke(Color.WHITE);
                colorCircle.setStrokeWidth(isCurrentPlayer ? 3 : 1);

                Label nameLabel = new Label(info.getName());
                nameLabel.setId("playerNameLabel_" + info.getName());
                nameLabel.setFont(Font.font("Press Start 2P", isCurrentPlayer ? 11 : 10));
                nameLabel.setTextFill(Color.WHITE);
                if (isCurrentPlayer) {
                    nameLabel.setStyle("-fx-font-weight: bold;");
                }

                Label aiIndicator = new Label();
                if (info.getIsAiControlled()) {
                    aiIndicator.setText("AI");
                    aiIndicator.setFont(Font.font(10));
                    aiIndicator.setTextFill(Color.CYAN);
                }

                Label connectionIndicator = new Label();
                if (!info.getIsConnected()) {
                    connectionIndicator.setText("!");
                    connectionIndicator.setFont(Font.font(12));
                    connectionIndicator.setTextFill(Color.ORANGE);
                }

                HBox playerRow = new HBox(8, colorCircle, nameLabel, aiIndicator, connectionIndicator);
                playerRow.setAlignment(Pos.CENTER_LEFT);
                playerRow.setPadding(new Insets(5, 5, 5, 5));

                if (isCurrentPlayer) {
                    playerRow.setStyle("-fx-background-color: rgba(76, 175, 80, 0.3); -fx-background-radius: 5;");
                }

                playerListBox.getChildren().add(playerRow);
                shown++;
            }

            // Mindestens 4 Zeilen auffüllen mit Platzhaltern "---"
            while (shown < 4) {
                Circle placeholderCircle = new Circle(8);
                placeholderCircle.setFill(Color.TRANSPARENT);
                placeholderCircle.setStroke(Color.rgb(255, 255, 255, 0.25));
                placeholderCircle.setStrokeWidth(1);

                Label placeholderLabel = new Label("---");
                placeholderLabel.setFont(Font.font("Press Start 2P", 10));
                //placeholderLabel.setTextFill(Color.rgb(255, 255, 255, 0.45));

                HBox placeholderRow = new HBox(8, placeholderCircle, placeholderLabel);
                placeholderRow.setAlignment(Pos.CENTER_LEFT);
                placeholderRow.setPadding(new Insets(5, 5, 5, 5));

                playerListBox.getChildren().add(placeholderRow);
                shown++;
            }
        });
    }
}
