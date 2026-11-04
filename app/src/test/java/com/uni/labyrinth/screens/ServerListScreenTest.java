package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.enums.Status;
import com.uni.labyrinth.model.GameServer;
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
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

public class ServerListScreenTest extends ApplicationTest {

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

    private ServerListScreen serverListScreen;
    private GameServer lobbyServer;

    @Start
    public void start(Stage stage) throws IOException, InterruptedException {
        MockitoAnnotations.openMocks(this);

        setupTestData();

        serverListScreen = new ServerListScreen(mainApp, gameServerService);
        stage.setScene(new Scene(serverListScreen.getRoot(), 1000, 800));
        stage.show();
    }

    private void setupTestData() throws IOException, InterruptedException {
        List<GameServer> servers = new ArrayList<>();

        lobbyServer = new GameServer();
        lobbyServer.setId(UUID.randomUUID());
        lobbyServer.setName("Test_Lobby");
        lobbyServer.setUri("ws://localhost:9000/game");
        lobbyServer.setMaxPlayers(4);
        lobbyServer.setCurrentPlayerCount(1);
        lobbyServer.setStatus(Status.LOBBY);

        GameServer runningServer = new GameServer();
        runningServer.setId(UUID.randomUUID());
        runningServer.setName("Running_Game");
        runningServer.setUri("play.example.com:9001");
        runningServer.setMaxPlayers(4);
        runningServer.setCurrentPlayerCount(3);
        runningServer.setStatus(Status.IN_GAME);

        servers.add(lobbyServer);
        servers.add(runningServer);

        when(gameServerService.getServerList()).thenReturn(servers);
    }

    @BeforeEach
    public void setUp() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDown() {
        if (serverListScreen != null) {
            serverListScreen.stop();
        }
        reset(gameServerService);
        reset(mainApp);
    }

    @Test
    public void ServerListScreen_initialContent() {
        verifyThat("#serverEntry_Test_Lobby", NodeMatchers.isVisible());
        verifyThat("#serverEntry_Running_Game", NodeMatchers.isVisible());

        verifyThat("#serverNameLabel_Test_Lobby", LabeledMatchers.hasText(containsString("Test_Lobby")));
        verifyThat("#statusLabel_Test_Lobby", LabeledMatchers.hasText(containsString("WARTEN AUF SPIELER")));
        verifyThat("#statusPlayerLabel_Test_Lobby", LabeledMatchers.hasText(containsString("1/4")));

        verifyThat("#serverNameLabel_Running_Game", LabeledMatchers.hasText(containsString("Running_Game")));
        verifyThat("#statusLabel_Running_Game", LabeledMatchers.hasText(containsString("SPIEL LÄUFT")));
        verifyThat("#statusPlayerLabel_Running_Game", LabeledMatchers.hasText(containsString("3/4")));
    }

    @Test
    public void ServerListScreen_refreshCalledInitially() throws IOException, InterruptedException {
        verify(gameServerService, atLeastOnce()).getServerList();
    }

    @Test
    public void ServerListScreen_successfulJoinWorkflowWithClick() {
        clickOn("#serverEntry_Test_Lobby");
        clickOn(".text-field");

        write("TestSpieler");
        clickOn("OK");

        verify(gameServerService, times(1)).handleConnectClick(eq(lobbyServer), eq(mainApp), eq("TestSpieler"));
    }

    @Test
    public void ServerListScreen_successfulJoinWorkflowWithKeyPressed() {
        clickOn("#serverEntry_Test_Lobby");
        clickOn(".text-field");

        write("TestSpieler");
        push(KeyCode.ENTER);

        verify(gameServerService, times(1)).handleConnectClick(eq(lobbyServer), eq(mainApp), eq("TestSpieler"));
    }

    @Test
    public void ServerListScreen_cancelJoinWorkflowWithClick() {
        clickOn("#serverEntry_Test_Lobby");

        clickOn("Abbrechen");

        verify(gameServerService, never()).handleConnectClick(any(GameServer.class), any(Main.class), anyString());
    }

    @Test
    public void ServerListScreen_cancelJoinWorkflowWithKeyPressed() {
        clickOn("#serverEntry_Test_Lobby");

        push(KeyCode.ESCAPE);

        verify(gameServerService, never()).handleConnectClick(any(GameServer.class), any(Main.class), anyString());
    }
}
