package com.uni.labyrinth.service;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.Coordinates;
import com.uni.labyrinth.model.GameServer;
import com.uni.labyrinth.model.LobbyState;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameServerServiceTest {

    private GameServerService gameServerService;
    @Mock
    private Session mockSession;
    @Mock
    private RemoteEndpoint.Async mockRemote;
    @Mock
    private Main mockMain;
    @Mock
    private LobbyState mockLobbyState;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        gameServerService = new GameServerService();

        when(mockSession.isOpen()).thenReturn(true);
        when(mockSession.getAsyncRemote()).thenReturn(mockRemote);

        java.lang.reflect.Field sessionField = GameServerService.class.getDeclaredField("currentSession");
        sessionField.setAccessible(true);
        sessionField.set(gameServerService, mockSession);
    }

    @Test
    void getServerList_returnsMockOnFailure() throws Exception {
        List<GameServer> servers = gameServerService.getServerList();
        assertNotNull(servers);
        assertFalse(servers.isEmpty(), "Sollte die Mock-Liste zurückgeben");
    }

    @Test
    void getMockServerList_containsRequiredData() {
        List<GameServer> servers = gameServerService.getMockServerList();
        assertEquals(2, servers.size());
        assertEquals("MCI Pro Labyrinth", servers.get(0).getName());
    }

    @Test
    void sendMovePlayer_sendsCorrectJson() {
        Coordinates coordinates = new Coordinates(5, 2);
        gameServerService.sendMovePlayer(coordinates);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockRemote).sendText(captor.capture());

        assertTrue(captor.getValue().contains("MOVE_PAWN"));
        assertTrue(captor.getValue().contains("\"column\":5"));
    }

    @Test
    void sendSwapCommand_sendsCorrectJson() {
        gameServerService.sendSwapCommand("player-123");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockRemote).sendText(captor.capture());
        assertTrue(captor.getValue().contains("player-123"));
    }

    @Test
    void sendPushTwiceCommand_sendsCommand() {
        gameServerService.sendPushTwiceCommand();
        verify(mockRemote).sendText(anyString());
    }

    @Test
    void sendBeamCommand_sendsCorrectJson() {
        Coordinates coordinates = new Coordinates(1, 1);
        gameServerService.sendBeamCommand(coordinates);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockRemote).sendText(captor.capture());
        assertTrue(captor.getValue().contains("column\":1"));
    }

    @Test
    void sendPushFixed_sendsCorrectJson() {
        gameServerService.sendPushFixed(3, Direction.LEFT);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockRemote).sendText(captor.capture());
        assertTrue(captor.getValue().contains("\"rowOrColIndex\":3"));
        assertTrue(captor.getValue().contains("\"direction\":\"LEFT\""));
    }

    @Test
    void sendPushTile_sendsCorrectJson() {
        gameServerService.sendPushTile(0, Direction.UP);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockRemote).sendText(captor.capture());
        assertTrue(captor.getValue().contains("UP"));
    }

    @Test
    void sendRotateCommand_sendsCommand() {
        gameServerService.sendRotateCommand();
        verify(mockRemote).sendText(anyString());
    }

    @Test
    void sendDisconnectCommand_cleansUpAndNavigates() throws Exception {
        when(mockMain.getLobbyState()).thenReturn(mockLobbyState);
        when(mockMain.getGameServerService()).thenReturn(gameServerService);

        gameServerService.sendDisconnectCommand(mockMain);

        verify(mockLobbyState).setIdentifierToken(null);
        verify(mockMain).resetCurrentGameScreenToNull();
        verify(mockSession).close();
    }

    @Test
    void getCurrentSession_returnsInjectedSession() {
        assertEquals(mockSession, gameServerService.getCurrentSession());
    }
}