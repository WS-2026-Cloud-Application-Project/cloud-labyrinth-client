package com.uni.labyrinth.ui;

import com.uni.labyrinth.domain.enums.BonusType;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.*;
import com.uni.labyrinth.service.GameServerService;
import com.uni.labyrinth.service.ImageCache;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class GameBoardView extends BorderPane {

    private final int tileSize = 70; // feste Größe
    private Tile[][] tiles;
    private TileClickListener tileClickListener;
    private PawnClickListener pawnClickListener;
    private final GridPane grid;
    private int remainingTreasureCount = 6;
    private Coordinates homePosition = new Coordinates();
    private String homeColor = "";

    private boolean pushButtonsAdded = false;
    private BonusType activeBonus = null;

    public GameBoardView(GameState gameState) {
        this.tiles = gameState.getBoard();
        this.grid = new GridPane();
        drawTiles();
        this.homeColor = gameState.getMyPlayerState().getPlayerInfo().getColor();
    }

    public void setRemainingTreasureCount(int remainingTreasureCount) {
        this.remainingTreasureCount = remainingTreasureCount;
    }

    public void setHomePosition(Coordinates homePosition) {
        this.homePosition = homePosition;
    }

    public void setTileClickListener(TileClickListener listener) {
        this.tileClickListener = listener;
    }

    public void setPawnClickListener(PawnClickListener listener) {
        this.pawnClickListener = listener;
    }

    // -------------------- Tiles erstellen --------------------
    public StackPane createTileImage(Tile tile, double tileSize) {
        String imagePath;
        String effectImagePath = "/images/shadow_glow.png";
        int rotation = 0;

        List<Direction> entrancesList = tile.getEntrances();
        if (entrancesList == null) entrancesList = new ArrayList<>();
        ArrayList<Direction> entrances = new ArrayList<>(entrancesList);
        Collections.sort(entrances);

        switch (entrances.size()) {
            case 2 -> { // straight oder corner
                if (entrances.containsAll(List.of(Direction.UP, Direction.DOWN)) || entrances.containsAll(List.of(Direction.LEFT, Direction.RIGHT))) {
                    imagePath = tile.isFixed() ? "/images/straight_tile_fixed.png" : "/images/straight_tile.png";
                    rotation = entrances.containsAll(List.of(Direction.LEFT, Direction.RIGHT)) ? 90 : 0;
                } else { // Corner-Tiles
                    imagePath = tile.isFixed() ? "/images/corner_tile_fixed.png" : "/images/corner_tile.png";
                    if (entrances.containsAll(List.of(Direction.UP, Direction.RIGHT))) rotation = 0;
                    else if (entrances.containsAll(List.of(Direction.RIGHT, Direction.DOWN))) rotation = 90;
                    else if (entrances.containsAll(List.of(Direction.DOWN, Direction.LEFT))) rotation = 180;
                    else if (entrances.containsAll(List.of(Direction.LEFT, Direction.UP))) rotation = 270;
                }
            }
            case 3 -> { // T-Tiles
                imagePath = tile.isFixed() ? "/images/t_tile_fixed.png" : "/images/t_tile.png";
                if (entrances.containsAll(List.of(Direction.UP, Direction.RIGHT, Direction.DOWN))) rotation = 0;
                else if (entrances.containsAll(List.of(Direction.RIGHT, Direction.DOWN, Direction.LEFT))) rotation = 90;
                else if (entrances.containsAll(List.of(Direction.UP, Direction.DOWN, Direction.LEFT))) rotation = 180;
                else if (entrances.containsAll(List.of(Direction.LEFT, Direction.RIGHT, Direction.UP))) rotation = 270;
            }
            default -> imagePath = "/images/shadow_glow.png";
        }

        ImageView imageView = new ImageView(ImageCache.get(getClass(), imagePath));
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);
        imageView.setRotate(rotation);

        ImageView effectView = new ImageView(ImageCache.get(getClass(), effectImagePath));
        effectView.setFitWidth(tileSize);
        effectView.setFitHeight(tileSize);

        if (tile.getTreasure() != null) {
            int treasureId = tile.getTreasure().getId();
            String treasurePath = "/images/treasure" + treasureId + ".png";
            ImageView treasureView = new ImageView(ImageCache.get(getClass(), treasurePath));
            treasureView.setFitWidth(tileSize);
            treasureView.setFitHeight(tileSize);

            return new StackPane(imageView, effectView, treasureView);
        }

        if (tile.getBonus() != null) {
            ImageView bonusView = new ImageView(ImageCache.get(getClass(), "/images/mario-coins.gif"));
            //FadeTransition blink = new FadeTransition(Duration.seconds(0.75), bonusView);
            //blink.setFromValue(0.0);
            //blink.setToValue(1.0);
            //blink.setCycleCount(Animation.INDEFINITE);
            //blink.setAutoReverse(true);
            //blink.play();

            bonusView.setFitWidth(tileSize*0.5);
            bonusView.setFitHeight(tileSize*0.5);

            return new StackPane(imageView, effectView, bonusView);
        }

        return new StackPane(imageView, effectView);
    }

    // -------------------- Tiles zeichnen --------------------
    public void drawTiles() {
        if (tiles == null || tiles.length == 0) return;

        grid.getChildren().clear();
        int rows = tiles.length;
        int cols = tiles[0].length;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Tile tile = tiles[y][x];
                StackPane tileView = createTileImage(tile, tileSize);

                StackPane cell = new StackPane(tileView);

                Pane homeBase = new Pane();

                if (remainingTreasureCount == 0 && x == homePosition.getColumn() && y == homePosition.getRow()) {
                    homeBase.setBorder(new Border(new BorderStroke(Color.valueOf(homeColor), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
                }

                cell.setId("tile_row" + y + "_col" + x);
                cell.getChildren().add(homeBase);
                cell.setPrefSize(tileSize, tileSize);

                int tileX = x;
                int tileY = y;

                cell.setOnMouseEntered(e -> cell.setCursor(Cursor.HAND));
                cell.setOnMouseExited(e -> cell.setCursor(Cursor.DEFAULT));
                cell.setOnMouseClicked(e -> {
                    if (tileClickListener != null) tileClickListener.onTileClicked(tileX, tileY);
                });

                grid.add(cell, x, y);
            }
        }

        setCenter(grid);
    }

    // -------------------- Push Buttons --------------------
    public void addPushButtons(GameServerService gameServerService) {
        if (pushButtonsAdded || tiles == null) return;
        pushButtonsAdded = true;

        int rows = tiles.length;
        int cols = tiles[0].length;
        Image arrowImage = new Image(getClass().getResource("/images/arrow.png").toExternalForm());

        // Helper für alle Richtungen
        BiConsumer<ImageView, int[]> setFlags = (iv, arr) -> iv.setUserData(arr);

        // --- Oben ---
        HBox top = new HBox(0);
        top.setAlignment(Pos.CENTER);

        for (int x = 0; x < cols; x++) {
            final int col = x;
            ImageView iv = createArrow(arrowImage, 0);

            boolean indexAllowed = x % 2 != 0;
            boolean isInner = x > 0 && x < cols - 1;

            setFlags.accept(iv, new int[]{indexAllowed ? 1 : 0, isInner ? 1 : 0});

            iv.setId("arrow_down_" + x);

            iv.setOnMouseClicked(e -> {
                if (activeBonus == null) gameServerService.sendPushTile(col, Direction.DOWN);
                else if (activeBonus == BonusType.PUSH_FIXED) {

                    if (indexAllowed) {
                        gameServerService.sendPushTile(col, Direction.DOWN);
                    } else {
                        gameServerService.sendPushFixed(col, Direction.DOWN);
                    }
                    setActiveBonus(null);
                    setPushButtonMode(PushButtonMode.NONE);
                }
            });
            top.getChildren().add(iv);
        }
        setTop(top);

        // --- Unten ---
        HBox bottom = new HBox(0);
        bottom.setAlignment(Pos.CENTER);

        for (int x = 0; x < cols; x++) {
            final int col = x;
            ImageView iv = createArrow(arrowImage, 180);

            boolean indexAllowed = x % 2 != 0;
            boolean isInner = x > 0 && x < cols - 1;
            setFlags.accept(iv, new int[]{indexAllowed ? 1 : 0, isInner ? 1 : 0});

            iv.setId("arrow_up_" + x);

            iv.setOnMouseClicked(e -> {
                if (activeBonus == null) gameServerService.sendPushTile(col, Direction.UP);
                else if (activeBonus == BonusType.PUSH_FIXED) {

                    if (indexAllowed) {
                        gameServerService.sendPushTile(col, Direction.UP);
                    } else {
                        gameServerService.sendPushFixed(col, Direction.UP);
                    }
                    setActiveBonus(null);
                    setPushButtonMode(PushButtonMode.NONE);
                }
            });

            bottom.getChildren().add(iv);
        }
        setBottom(bottom);

        // --- Links ---
        VBox left = new VBox(0);
        left.setAlignment(Pos.CENTER);

        for (int y = 0; y < rows; y++) {
            final int row = y;
            ImageView iv = createArrow(arrowImage, 270);

            boolean indexAllowed = y % 2 != 0;
            boolean isInner = y > 0 && y < rows - 1;
            setFlags.accept(iv, new int[]{indexAllowed ? 1 : 0, isInner ? 1 : 0});

            iv.setId("arrow_left_" + y);

            iv.setOnMouseClicked(e -> {
                if (activeBonus == null) gameServerService.sendPushTile(row, Direction.RIGHT);
                else if (activeBonus == BonusType.PUSH_FIXED) {

                    if (indexAllowed) {
                        gameServerService.sendPushTile(row, Direction.RIGHT);
                    } else {
                        gameServerService.sendPushFixed(row, Direction.RIGHT);
                    }
                    setActiveBonus(null);
                    setPushButtonMode(PushButtonMode.NONE);
                }
            });

            left.getChildren().add(iv);
        }
        setLeft(left);

        // --- Rechts ---
        VBox right = new VBox(0);
        right.setAlignment(Pos.CENTER);

        for (int y = 0; y < rows; y++) {
            final int row = y;
            ImageView iv = createArrow(arrowImage, 90);

            boolean indexAllowed = y % 2 != 0;
            boolean isInner = y > 0 && y < rows - 1;
            setFlags.accept(iv, new int[]{indexAllowed ? 1 : 0, isInner ? 1 : 0});

            iv.setId("arrow_right_" + y);

            iv.setOnMouseClicked(e -> {
                if (activeBonus == null) gameServerService.sendPushTile(row, Direction.LEFT);
                else if (activeBonus == BonusType.PUSH_FIXED) {
                    if (indexAllowed) {
                        gameServerService.sendPushTile(row, Direction.LEFT);
                    } else {
                        gameServerService.sendPushFixed(row, Direction.LEFT);
                    }
                    setActiveBonus(null);
                    setPushButtonMode(PushButtonMode.NONE);
                }
            });

            right.getChildren().add(iv);
        }
        setRight(right);

        setPushButtonMode(PushButtonMode.NONE);
    }

    public void setPushButtonMode(PushButtonMode mode) {
        Node[] containers = new Node[]{getTop(), getBottom(), getLeft(), getRight()};

        for (Node containerNode : containers) {
            if (!(containerNode instanceof Pane container)) continue;

            for (Node node : container.getChildren()) {
                if (!(node instanceof ImageView iv)) continue;

                int[] flags = (int[]) iv.getUserData();
                boolean indexAllowed = flags[0] == 1;
                boolean isInner = flags[1] == 1;

                switch (mode) {
                    case NONE -> {
                        iv.setVisible(false);
                        iv.setDisable(true);
                    }
                    case NORMAL -> {
                        iv.setVisible(indexAllowed);
                        iv.setDisable(!indexAllowed);
                    }
                    case ALL -> {
                        iv.setVisible(isInner);
                        iv.setDisable(!isInner);
                    }
                }
            }
        }
    }

    private ImageView createArrow(Image image, int rotation) {
        ImageView iv = new ImageView(image);
        iv.setRotate(rotation);
        iv.setFitWidth(tileSize);
        iv.setFitHeight(tileSize);
        iv.setOnMouseEntered(e -> iv.setCursor(Cursor.HAND));
        iv.setOnMouseExited(e -> iv.setCursor(Cursor.DEFAULT));
        iv.setPreserveRatio(true);
        iv.setPickOnBounds(true);
        iv.setVisible(false);
        iv.setDisable(true);
        return iv;
    }

    // -------------------- Pawns zeichnen --------------------
    public void drawAllPawns(List<PlayerState> players) {
        for (var node : grid.getChildren()) {
            if (node instanceof StackPane cell) {
                cell.getChildren().removeIf(n -> n instanceof Circle);
            }
        }
        for (PlayerState ps : players) drawPawn(ps);
    }

    private void drawPawn(PlayerState playerState) {
        PlayerInfo player = playerState.getPlayerInfo();
        Coordinates pos = playerState.getCurrentPosition();
        String playerId = player.getId();

        int col = pos.getColumn();
        int row = pos.getRow();

        StackPane cell = getCell(col, row);
        if (cell == null) return;

        Circle pawn = new Circle(tileSize / 4);
        pawn.setId("pawn" + playerId);
        pawn.setFill(Color.web(player.getColor()));
        pawn.setStroke(Color.WHITE);
        pawn.setStrokeWidth(2);

        pawn.setOnMouseClicked(e -> {
            if (pawnClickListener != null) pawnClickListener.onPawnClicked(playerId);
        });

        cell.getChildren().add(pawn);
    }

    // -------------------- Hilfsmethode --------------------
    private StackPane getCell(int x, int y) {
        for (var node : grid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            if (col == null) col = 0;
            if (row == null) row = 0;
            if (col == x && row == y) return (StackPane) node;
        }
        return null;
    }

    public void setActiveBonus(BonusType activeBonusType) {
        this.activeBonus = activeBonusType;
    }

    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    public enum PushButtonMode {
        NONE,       // kein Spieler am Zug
        NORMAL,     // nur jeder zweite Button sichtbar
        ALL         // alle Buttons sichtbar (Bonus / Spezialfall)
    }

    public interface TileClickListener {
        void onTileClicked(int x, int y);
    }

    public interface PawnClickListener {
        void onPawnClicked(String playerId);
    }
}