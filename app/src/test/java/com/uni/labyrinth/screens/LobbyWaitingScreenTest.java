package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.model.LobbyState;
import com.uni.labyrinth.model.PlayerInfo;
import com.uni.labyrinth.service.GameServerService;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

public class LobbyWaitingScreenTest extends ApplicationTest {

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

    private LobbyState lobbyState;
    private List<PlayerInfo> players;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        setupTestData();

        LobbyWaitingScreen lobbyWaitingScreen = new LobbyWaitingScreen(mainApp);
        stage.setScene(new Scene(lobbyWaitingScreen.getRoot(), 800, 600));
        stage.show();
    }

    private void setupTestData() {
        PlayerInfo adminPlayer = new PlayerInfo("1", "Admin", "red", true, false, true);
        PlayerInfo normalPlayer = new PlayerInfo("2", "NormalUser", "blue", false, false, true);

        players = new ArrayList<>(List.of(adminPlayer, normalPlayer));

        lobbyState = new LobbyState();
        lobbyState.setPlayers(players);

        when(mainApp.getLobbyState()).thenReturn(lobbyState);
        when(mainApp.getGameServerService()).thenReturn(gameServerService);

        doCallRealMethod().when(gameServerService).sendStartGameCommand(anyInt(), any());
    }

    private void updateLobbyState() {
        interact(() -> lobbyState.setPlayers(players));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    public void setUp() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() {
        reset(gameServerService);
        reset(mainApp);
    }

    @Test
    public void LobbyWaitingScreen_initialContent() {
        verifyThat("#waitingText", org.testfx.matcher.control.TextMatchers.hasText(containsString("2/4")));

        verifyThat("#leaveLobbyButton", NodeMatchers.isVisible());
        verifyThat("#startGameButton", NodeMatchers.isInvisible());
    }

    @Test
    public void LobbyWaitingScreen_updatePlayerCountText() {
        players.add(new PlayerInfo("3", "Player3", "green", false, false, true));
        updateLobbyState();

        verifyThat("#waitingText", org.testfx.matcher.control.TextMatchers.hasText(containsString("3/4")));

        players.add(new PlayerInfo("4", "Player4", "yellow", false, false, true));
        updateLobbyState();

        verifyThat("#waitingText", org.testfx.matcher.control.TextMatchers.hasText(containsString("4/4")));
    }

    @Test
    public void LobbyWaitingScreen_adminSeesStartButton() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        verifyThat("#startGameButton", NodeMatchers.isVisible());
        verifyThat("#startGameButton", NodeMatchers.isEnabled());
    }

    @Test
    public void LobbyWaitingScreen_nonAdminDoesNotSeeStartButton() {
        lobbyState.setMyPlayerId("2");
        updateLobbyState();

        verifyThat("#startGameButton", NodeMatchers.isInvisible());
    }

    @Test
    public void LobbyWaitingScreen_cancelStartGameWorkflowWithClick() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        verify(gameServerService, times(1)).sendStartGameCommand(anyInt(), any());

        clickOn("#cancelButton");
    }

    @Test
    public void LobbyWaitingScreen_cancelStartGameWorkflowWithKeyPressed() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        verify(gameServerService, times(1)).sendStartGameCommand(anyInt(), any());

        push(KeyCode.ESCAPE);
    }

    @Test
    public void LobbyWaitingScreen_startGameWorkflowWithClick() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        verify(gameServerService, times(1)).sendStartGameCommand(anyInt(), any());

        clickOn("#sizeField").write("5");
        clickOn("#treasureField").write("3");
        clickOn("#timeField").write("6");
        clickOn("#bonusField").write("5");

        clickOn("#startButton");
    }

    @Test
    public void LobbyWaitingScreen_startGameWorkflowWithKeyPressed() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        verify(gameServerService, times(1)).sendStartGameCommand(anyInt(), any());

        clickOn("#sizeField").write("5");
        clickOn("#treasureField").write("3");
        clickOn("#timeField").write("6");
        clickOn("#bonusField").write("5");

        push(KeyCode.ENTER);
    }

    @Test
    public void LobbyWaitingScreen_startGameWorkflowWithKeyPressOnly() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        verify(gameServerService, times(1)).sendStartGameCommand(anyInt(), any());

        push(KeyCode.ENTER);
        push(KeyCode.ENTER);
        push(KeyCode.ENTER);
        push(KeyCode.ENTER);
    }

    @Test
    public void LobbyWaitingScreen_startGameWorkflowWithErrorBoardSize() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        clickOn("#sizeField").write("6");
        clickOn("#treasureField").write("3");
        clickOn("#timeField").write("6");
        clickOn("#bonusField").write("5");
        clickOn("#startButton");

        verifyThat("#errorLabel", NodeMatchers.isVisible());
        verifyThat("#errorLabel", org.testfx.matcher.control.LabeledMatchers.hasText(containsString("Boardgröße muss eine ungerade Zahl zwischen 3 und 11 sein!")));

        clickOn("Abbrechen");
    }

    @Test
    public void LobbyWaitingScreen_startGameWorkflowWithErrorTreasures() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");

        clickOn("#sizeField").write("7");
        clickOn("#treasureField").write("0");
        clickOn("#timeField").write("6");
        clickOn("#bonusField").write("5");
        clickOn("#startButton");

        verifyThat("#errorLabel", NodeMatchers.isVisible());
        verifyThat("#errorLabel", org.testfx.matcher.control.LabeledMatchers.hasText(containsString("Schatzkarten müssen >= 1 sein!")));

        clickOn("Abbrechen");
    }

    @Test
    public void LobbyWaitingScreen_startGameWorkflowWithErrorEmptyFields() {
        lobbyState.setMyPlayerId("1");
        updateLobbyState();

        clickOn("#startGameButton");
        clickOn("#startButton");

        verifyThat("#errorLabel", NodeMatchers.isVisible());
        verifyThat("#errorLabel", org.testfx.matcher.control.LabeledMatchers.hasText(containsString("Bitte gültige Zahlen eingeben!")));

        clickOn("Abbrechen");
    }

    @Test
    public void LobbyWaitingScreen_leaveLobbyClick() {
        clickOn("#leaveLobbyButton");

        verify(gameServerService, times(1)).sendDisconnectCommand(any(Main.class));
    }
}