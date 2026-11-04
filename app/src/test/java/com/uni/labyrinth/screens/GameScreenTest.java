package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.enums.BonusType;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.domain.enums.TurnState;
import com.uni.labyrinth.model.*;
import com.uni.labyrinth.service.GameServerService;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

public class GameScreenTest extends ApplicationTest {

    static {
        if ("true".equalsIgnoreCase(System.getProperty("headless"))) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }

    @Mock
    private Main mainApp;
    @Mock
    private GameServerService gameServerService;

    private GameState gameState;
    private PlayerState myPlayerState;

    private GameScreen gameScreen;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        setupTestData();

        gameScreen = new GameScreen(gameState, gameServerService, mainApp);
        stage.setScene(new Scene(gameScreen.getRoot(), 1200, 800));
        stage.show();
    }

    private void setupTestData() {
        PlayerInfo myPlayerInfo = new PlayerInfo("1", "testUser", "red", true, false, true);
        myPlayerState = new PlayerState();
        myPlayerState.setPlayer(myPlayerInfo);
        myPlayerState.setRemainingTreasureCount(3);
        myPlayerState.setTreasuresFound(new ArrayList<>());
        myPlayerState.setCurrentTreasure(1, "Test");
        myPlayerState.setCurrentPosition(new Coordinates(0, 0));

        PlayerInfo secondPlayerInfo = new PlayerInfo("2", "testUser2", "green", false, false, true);
        PlayerState secondPlayerState = new PlayerState();
        secondPlayerState.setPlayer(secondPlayerInfo);
        secondPlayerState.setRemainingTreasureCount(7);
        secondPlayerState.setTreasuresFound(new ArrayList<>());
        secondPlayerState.setCurrentTreasure(0, "Test2");
        secondPlayerState.setCurrentPosition(new Coordinates(6, 0));

        // Mock Board (7x7)
        Tile[][] board = new Tile[7][7];
        for (int i = 0; i < 7; i++) for (int j = 0; j < 7; j++) board[i][j] = new Tile();

        gameState = new GameState();
        gameState.setCurrentPlayer(myPlayerState);
        gameState.setBoard(board);
        gameState.setMyPlayerId("1");
        gameState.setCurrentPlayerId("1");
        gameState.setPlayers(List.of(myPlayerState, secondPlayerState));
        gameState.setCurrentTreasure(1, "Test");
        gameState.setExtraTile(new Tile(List.of(Direction.UP, Direction.DOWN), false, null, null));

        LobbyState lobbyState = new LobbyState();
        lobbyState.setPlayers(List.of(myPlayerInfo, secondPlayerInfo));
        lobbyState.setMyPlayerId("1");

        when(mainApp.getLobbyState()).thenReturn(lobbyState);
    }

    private void updateGameState(Runnable action) {
        interact(action);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    public void setUp() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() {
        if (gameScreen != null) {
            gameScreen.disableAutoAI();
        }

        reset(gameServerService);
        reset(mainApp);
    }

    @Test
    public void GameScreen_labelsInitialContent() {
        verifyThat("#screenHeader", LabeledMatchers.hasText(containsString("Mario's verrücktes Labyrinth")));
        verifyThat("#playerNameLabel_testUser", LabeledMatchers.hasText(containsString("testUser")));
        verifyThat("#treasuresFoundAndTotalLabel", LabeledMatchers.hasText(containsString("0 / 3")));
    }

    @Test
    public void GameScreen_rotateButtonClick() {
        clickOn("#rotateButton");

        verify(gameServerService, times(1)).sendRotateCommand();
    }

    @Test
    public void GameScreen_arrowsVisibilityWaitingForMove() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_MOVE));

        // Beispiel für eine bewegliche Spalte
        verifyThat("#arrow_down_1", NodeMatchers.isInvisible());
        verifyThat("#arrow_down_1", NodeMatchers.isDisabled());

        // Beispiel für eine feste Spalte
        verifyThat("#arrow_down_2", NodeMatchers.isInvisible());
        verifyThat("#arrow_down_2", NodeMatchers.isDisabled());
    }

    @Test
    public void GameScreen_arrowsVisibilityWaitingForPush() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        // Beispiel für eine bewegliche Spalte
        verifyThat("#arrow_down_1", NodeMatchers.isVisible());
        verifyThat("#arrow_down_1", NodeMatchers.isEnabled());

        // Beispiel für eine feste Spalte
        verifyThat("#arrow_down_2", NodeMatchers.isInvisible());
        verifyThat("#arrow_down_2", NodeMatchers.isDisabled());
    }

    @Test
    public void GameScreen_arrowDownClickSendsPushCommand() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        clickOn("#arrow_down_1");

        verify(gameServerService, times(1)).sendPushTile(1, Direction.DOWN);
    }

    @Test
    public void GameScreen_arrowUpClickSendsPushCommand() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        clickOn("#arrow_up_1");

        verify(gameServerService, times(1)).sendPushTile(1, Direction.UP);
    }

    @Test
    public void GameScreen_arrowLeftClickSendsPushCommand() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        clickOn("#arrow_left_1");

        verify(gameServerService, times(1)).sendPushTile(1, Direction.RIGHT);
    }

    @Test
    public void GameScreen_arrowRightClickSendsPushCommand() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        clickOn("#arrow_right_1");

        verify(gameServerService, times(1)).sendPushTile(1, Direction.LEFT);
    }

    @Test
    public void GameScreen_tileClickSendsMoveCommand() {
        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_MOVE));

        clickOn("#tile_row0_col2");

        verify(gameServerService, times(1)).sendMovePlayer(any(Coordinates.class));
    }

    @Test
    public void GameScreen_bonusButtonVisibility() {
        verifyThat("#bonusButtonBEAM", NodeMatchers.isVisible());
        verifyThat("#bonusButtonBEAM", NodeMatchers.isDisabled());

        verifyThat("#bonusButtonPUSH_FIXED", NodeMatchers.isVisible());
        verifyThat("#bonusButtonPUSH_FIXED", NodeMatchers.isDisabled());

        verifyThat("#bonusButtonSWAP", NodeMatchers.isVisible());
        verifyThat("#bonusButtonSWAP", NodeMatchers.isDisabled());

        verifyThat("#bonusButtonPUSH_TWICE", NodeMatchers.isVisible());
        verifyThat("#bonusButtonPUSH_TWICE", NodeMatchers.isDisabled());
    }

    @Test
    public void GameScreen_bonusBeamActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.BEAM));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonBEAM", NodeMatchers.isEnabled());

        clickOn("#bonusButtonBEAM");
        clickOn("#tile_row2_col3");

        verify(gameServerService, times(1)).sendBeamCommand(any(Coordinates.class));
    }

    @Test
    public void GameScreen_bonusPushFixedDownActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.PUSH_FIXED));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonPUSH_FIXED", NodeMatchers.isEnabled());

        clickOn("#bonusButtonPUSH_FIXED");
        clickOn("#arrow_down_2");

        verify(gameServerService, times(1)).sendPushFixed(2, Direction.DOWN);
    }

    @Test
    public void GameScreen_bonusPushFixedUpActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.PUSH_FIXED));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonPUSH_FIXED", NodeMatchers.isEnabled());

        clickOn("#bonusButtonPUSH_FIXED");
        clickOn("#arrow_up_2");

        verify(gameServerService, times(1)).sendPushFixed(2, Direction.UP);
    }

    @Test
    public void GameScreen_bonusPushFixedLeftActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.PUSH_FIXED));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonPUSH_FIXED", NodeMatchers.isEnabled());

        clickOn("#bonusButtonPUSH_FIXED");
        clickOn("#arrow_left_2");

        verify(gameServerService, times(1)).sendPushFixed(2, Direction.RIGHT);
    }

    @Test
    public void GameScreen_bonusPushFixedRightActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.PUSH_FIXED));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonPUSH_FIXED", NodeMatchers.isEnabled());

        clickOn("#bonusButtonPUSH_FIXED");
        clickOn("#arrow_right_2");

        verify(gameServerService, times(1)).sendPushFixed(2, Direction.LEFT);
    }

    @Test
    public void GameScreen_bonusSwapActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.SWAP));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonSWAP", NodeMatchers.isEnabled());

        clickOn("#bonusButtonSWAP");
        clickOn("#pawn2");

        verify(gameServerService, times(1)).sendSwapCommand(anyString());
    }

    @Test
    public void GameScreen_bonusPushTwiceActivation() {
        myPlayerState.setAvailableBonuses(List.of(BonusType.PUSH_TWICE));

        updateGameState(() -> gameState.setCurrentTurnState(TurnState.WAITING_FOR_PUSH));

        verifyThat("#bonusButtonPUSH_TWICE", NodeMatchers.isEnabled());

        clickOn("#bonusButtonPUSH_TWICE");

        verify(gameServerService, times(1)).sendPushTwiceCommand();
    }

    @Test
    public void GameScreen_menuButtonDisconnectClick() {
        verifyThat("#menuButton", NodeMatchers.isEnabled());
        clickOn("#menuButton");

        verifyThat("#disconnectButton", NodeMatchers.isEnabled());
        clickOn("#disconnectButton");

        verify(gameServerService, times(1)).sendDisconnectCommand(any(Main.class));
    }

    @Test
    public void GameScreen_menuButtonHelpClick() {
        verifyThat("#menuButton", NodeMatchers.isEnabled());
        clickOn("#menuButton");

        verifyThat("#helpButton", NodeMatchers.isEnabled());
        clickOn("#helpButton");

        verifyThat("#nextButton", NodeMatchers.isEnabled());
        verifyThat("#backButton", NodeMatchers.isDisabled());

        clickOn("#nextButton");

        verifyThat("#nextButton", NodeMatchers.isEnabled());
        verifyThat("#backButton", NodeMatchers.isEnabled());

        clickOn("#backButton");

        verifyThat("#nextButton", NodeMatchers.isEnabled());
        verifyThat("#backButton", NodeMatchers.isDisabled());
    }

    @Test
    public void GameScreen_aiButtonDisabledWhenAutoAIActive() {
        Button aiButton = lookup("#aiButton").queryAs(Button.class);

        verifyThat("#aiButton", NodeMatchers.isEnabled());
        assertEquals("KI-Zug", aiButton.getText());

        clickOn("#aiAutoButton");

        verifyThat("#aiButton", NodeMatchers.isDisabled());
        assertEquals("KI denkt...", aiButton.getText());
    }

    @Test
    public void GameScreen_aiAutoButtonToggleTextChanges() {
        ToggleButton aiAutoButton = lookup("#aiAutoButton").queryAs(ToggleButton.class);

        assertEquals("Auto-KI: AUS", aiAutoButton.getText());

        clickOn(aiAutoButton);
        assertEquals("Auto-KI: AN", aiAutoButton.getText());

        clickOn(aiAutoButton);
        assertEquals("Auto-KI: AUS", aiAutoButton.getText());
    }

    @Test
    public void GameScreen_afkModeImBackButtonAppears() {
        verifyThat("#imBackButton", NodeMatchers.isInvisible());

        interact(() -> {
            try {
                java.lang.reflect.Method method = GameScreen.class.getDeclaredMethod("activateAFKMode");
                method.setAccessible(true);
                method.invoke(gameScreen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        verifyThat("#imBackButton", NodeMatchers.isVisible());
        assertTrue(lookup("#aiAutoButton").queryAs(ToggleButton.class).isSelected());
    }

    @Test
    public void GameScreen_imBackButtonDeactivatesAFKAndAutoAI() {
        interact(() -> {
            try {
                java.lang.reflect.Method method = GameScreen.class.getDeclaredMethod("activateAFKMode");
                method.setAccessible(true);
                method.invoke(gameScreen);
            } catch (Exception ignored) {
            }
        });

        clickOn("#imBackButton");

        verifyThat("#imBackButton", NodeMatchers.isInvisible());
        assertFalse(lookup("#aiAutoButton").queryAs(ToggleButton.class).isSelected());
    }
}
