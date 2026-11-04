package com.uni.labyrinth.screens;

import com.uni.labyrinth.Main;
import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.model.*;
import com.uni.labyrinth.service.GameServerService;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
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
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextMatchers.hasText;

public class ResultScreenTest extends ApplicationTest {

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
    @Mock
    private GameState gameState;

    private List<RankingEntry> mockRanking;

    @Start
    public void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        setupTestData();

        ResultScreen resultScreen = new ResultScreen(mainApp, "1", mockRanking, gameState);
        stage.setScene(new Scene(resultScreen.getRoot(), 1000, 800));
        stage.show();
    }

    private void setupTestData() {
        PlayerInfo winnerInfo = new PlayerInfo("1", "Mario", "red", true, false, true);
        PlayerState winnerState = mock(PlayerState.class);
        when(winnerState.getPlayerInfo()).thenReturn(winnerInfo);
        when(winnerState.getAvailableAchievements()).thenReturn(List.of(AchievementType.RUNNER, AchievementType.PUSHER));

        PlayerInfo loserInfo = new PlayerInfo("2", "Luigi", "blue", false, false, true);
        PlayerState loserState = mock(PlayerState.class);
        when(loserState.getPlayerInfo()).thenReturn(loserInfo);
        when(loserState.getAvailableAchievements()).thenReturn(null);

        when(gameState.getPlayerById("1")).thenReturn(winnerState);
        when(gameState.getPlayerById("2")).thenReturn(loserState);

        mockRanking = new ArrayList<>();

        PlayerGameStats winnerStats = new PlayerGameStats(120, 20, 5);
        mockRanking.add(new RankingEntry("1", 1, 1500, winnerStats));

        PlayerGameStats loserStats = new PlayerGameStats(100, 20, 1);
        mockRanking.add(new RankingEntry("2", 2, 800, loserStats));

        LobbyState lobbyState = new LobbyState();
        when(mainApp.getLobbyState()).thenReturn(lobbyState);
        when(mainApp.getGameServerService()).thenReturn(gameServerService);
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
    public void ResultScreen_winnerLabel() {
        verifyThat("#winnerLabel", hasText(containsString("Mario")));

        Text winnerLabel = lookup("#winnerLabel").queryAs(Text.class);

        assert (winnerLabel.getFill().equals(Color.web("red")));
    }

    @Test
    public void ResultScreen_rankingDetails() {
        verifyThat("#rankingRow_1", NodeMatchers.isVisible());
        verifyThat("#rankingRow_2", NodeMatchers.isVisible());

        verifyThat("#rankLabel_1", LabeledMatchers.hasText(containsString("1")));
        verifyThat("#nameLabel_1", LabeledMatchers.hasText(containsString("Mario")));
        verifyThat("#scoreLabel_1", LabeledMatchers.hasText(containsString("1500")));
        verifyThat("#treasuresLabel_1", LabeledMatchers.hasText(containsString("5")));
        verifyThat("#stepsLabel_1", LabeledMatchers.hasText(containsString("120")));
        verifyThat("#achievementTypeLabel_1", LabeledMatchers.hasText(containsString("PUSHER")));

        Circle colorCircle = lookup("#colorCircle_1").queryAs(Circle.class);
        assert (colorCircle.getFill().equals(Color.web("red")));

        verifyThat("#rankLabel_2", LabeledMatchers.hasText(containsString("2")));
        verifyThat("#nameLabel_2", LabeledMatchers.hasText(containsString("Luigi")));
        verifyThat("#scoreLabel_2", LabeledMatchers.hasText(containsString("800")));
        verifyThat("#treasuresLabel_2", LabeledMatchers.hasText(containsString("1")));
        verifyThat("#stepsLabel_2", LabeledMatchers.hasText(containsString("100")));
        verifyThat("#achievementTypeLabel_2", LabeledMatchers.hasText(containsString("Mamma mia...\ndiesmal nicht!")));

        colorCircle = lookup("#colorCircle_2").queryAs(Circle.class);
        assert (colorCircle.getFill().equals(Color.web("blue")));
    }

    @Test
    public void ResultScreen_backButtonClick() {
        clickOn("#backBtn");

        verify(gameServerService, times(1)).sendDisconnectCommand(any(Main.class));
    }

    @Test
    public void ResultScreen_lobbyButtonClick() {
        clickOn("#lobbyBtn");

        verify(mainApp, times(1)).resetCurrentGameScreenToNull();
        verify(mainApp, atLeastOnce()).getLobbyState();
        verify(mainApp, times(1)).setScreen(any(Pane.class));
    }
}
