package com.uni.labyrinth.service.websocket;

import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.domain.enums.TurnState;
import com.uni.labyrinth.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameWebSocketClientTest {

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void onMessage_GAME_STATE_UPDATE_callsOnGameStateUpdateWithTurnInfo() {
        GameClientListener listener = mock(GameClientListener.class);
        GameWebSocketClient client = new GameWebSocketClient(listener);

        String json = """
                {"type":"GAME_STATE_UPDATE","board":{"rows":7,"cols":7,"tiles":[[{"entrances":["RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["UP","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["LEFT","DOWN"],"treasure":null,"bonus":null,"isFixed":true}],[{"entrances":["LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT"],"treasure":{"id":1,"name":"Treasure 1"},"bonus":null,"isFixed":false},{"entrances":["LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","DOWN"],"treasure":{"id":4,"name":"Treasure 4"},"bonus":null,"isFixed":false},{"entrances":["UP","DOWN"],"treasure":{"id":2,"name":"Treasure 2"},"bonus":null,"isFixed":false},{"entrances":["UP","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false}],[{"entrances":["UP","RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":true}],[{"entrances":["UP","RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":false}],[{"entrances":["UP","RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT","DOWN"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["UP","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["UP","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","DOWN","LEFT"],"treasure":{"id":3,"name":"Treasure 3"},"bonus":null,"isFixed":true}],[{"entrances":["DOWN","LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["RIGHT","DOWN","LEFT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","DOWN"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["LEFT","UP"],"treasure":null,"bonus":null,"isFixed":false}],[{"entrances":["UP","RIGHT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT","LEFT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["UP","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","RIGHT","LEFT"],"treasure":null,"bonus":null,"isFixed":true},{"entrances":["UP","RIGHT"],"treasure":null,"bonus":null,"isFixed":false},{"entrances":["UP","LEFT"],"treasure":null,"bonus":null,"isFixed":true}]],"lastPush":null,"spareTile":{"entrances":["UP","LEFT","RIGHT"],"treasure":null,"bonus":null,"isFixed":false}},"players":[{"playerInfo":{"id":"a0a11295-066e-6794-b907-efd9522a0584","name":"yxcv","color":"RED","isAdmin":true,"isAiControlled":false,"isConnected":true},"currentPosition":{"column":0,"row":0},"homePosition":{"column":0,"row":0},"treasuresFound":[],"remainingTreasureCount":2,"achievements":[],"availableBonuses":[],"amountOfAchievements":0},{"playerInfo":{"id":"d32f604f-53a7-9db4-58b5-6bb68fc44121","name":"y","color":"GREEN","isAdmin":false,"isAiControlled":false,"isConnected":true},"currentPosition":{"column":6,"row":0},"homePosition":{"column":6,"row":0},"treasuresFound":[],"remainingTreasureCount":2,"achievements":[],"availableBonuses":[],"amountOfAchievements":0}],"currentTurnInfo":{"currentPlayerId":"a0a11295-066e-6794-b907-efd9522a0584","turnEndTime":"2026-01-17T17:30:54.005486995Z","turnState":"WAITING_FOR_PUSH"},"gameEndTime":"2026-01-17T17:39:54.005272701Z"}
                """;

        client.onMessage(json);

        ArgumentCaptor<Tile[][]> tilesCaptor = ArgumentCaptor.forClass(Tile[][].class);
        ArgumentCaptor<Tile> extraTileCaptor = ArgumentCaptor.forClass(Tile.class);
        ArgumentCaptor<TurnState> currentTurnStateCaptor = ArgumentCaptor.forClass(TurnState.class);
        ArgumentCaptor<String> currentPlayerIdCaptor = ArgumentCaptor.forClass(String.class);

        @SuppressWarnings("unchecked") ArgumentCaptor<List<PlayerState>> playersCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<LastPush> lastPushCaptor = ArgumentCaptor.forClass(LastPush.class);
        ArgumentCaptor<String> turnEndTimeCaptor = ArgumentCaptor.forClass(String.class);

        verify(listener).onGameStateUpdate(tilesCaptor.capture(), extraTileCaptor.capture(), currentTurnStateCaptor.capture(), currentPlayerIdCaptor.capture(), playersCaptor.capture(), lastPushCaptor.capture(), turnEndTimeCaptor.capture());

        assertEquals(7, tilesCaptor.getValue().length);
        assertNotNull(extraTileCaptor.getValue());
        assertEquals(TurnState.WAITING_FOR_PUSH, currentTurnStateCaptor.getValue());
        assertEquals("a0a11295-066e-6794-b907-efd9522a0584", currentPlayerIdCaptor.getValue());
        assertEquals(2, playersCaptor.getValue().size());
        assertNull(lastPushCaptor.getValue());

        verifyNoMoreInteractions(listener);
    }

    @Test
    void onMessage_PLAYER_TURN_callsOnPlayerTurn() {
        GameClientListener listener = mock(GameClientListener.class);
        GameWebSocketClient client = new GameWebSocketClient(listener);

        String json = """
                {
                  "type": "PLAYER_TURN",
                  "playerId": "p1",
                  "extraTile": {
                    "entrances": ["UP","LEFT","RIGHT"],
                    "treasure": null,
                    "bonus": null,
                    "isFixed": false
                  }
                }
                """;

        client.onMessage(json);

        ArgumentCaptor<String> playerIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Tile> tileCaptor = ArgumentCaptor.forClass(Tile.class);

        verify(listener).onPlayerTurn(playerIdCaptor.capture(), tileCaptor.capture());

        assertEquals("p1", playerIdCaptor.getValue());
        assertNotNull(tileCaptor.getValue());
        assertFalse(tileCaptor.getValue().isFixed());
        assertTrue(tileCaptor.getValue().getEntrances().contains(Direction.UP));

        verifyNoMoreInteractions(listener);
    }

    @Test
    void onMessage_GAME_OVER_callsOnGameOver() {
        GameClientListener listener = mock(GameClientListener.class);
        GameWebSocketClient client = new GameWebSocketClient(listener);

        String json = """
                {
                  "type": "GAME_OVER",
                  "winnerId": "winner-123",
                  "ranking": [
                    { "playerId": "winner-123", "rank": 1, "score": 10 },
                    { "playerId": "loser-456",  "rank": 2, "score": 5 }
                  ]
                }
                """;

        client.onMessage(json);

        ArgumentCaptor<String> winnerCaptor = ArgumentCaptor.forClass(String.class);

        @SuppressWarnings("unchecked") ArgumentCaptor<List<RankingEntry>> rankingCaptor = ArgumentCaptor.forClass(List.class);

        verify(listener).onGameOver(winnerCaptor.capture(), rankingCaptor.capture());

        assertEquals("winner-123", winnerCaptor.getValue());
        assertNotNull(rankingCaptor.getValue());
        assertEquals(2, rankingCaptor.getValue().size());

        verifyNoMoreInteractions(listener);
    }

    @Test
    void onMessage_NEXT_TREASURE_callsOnNextTreasure() {
        GameClientListener listener = mock(GameClientListener.class);
        GameWebSocketClient client = new GameWebSocketClient(listener);

        String json = """
                {
                  "type": "NEXT_TREASURE",
                  "treasure": { "id": 3, "name": "Treasure 3" }
                }
                """;

        client.onMessage(json);

        verify(listener).onNextTreasure(3, "Treasure 3");
        verifyNoMoreInteractions(listener);
    }

    @Test
    void buildTiles_shouldCreateNewTileArrayWithSameData() throws Exception {
        GameClientListener listener = mock(GameClientListener.class);
        GameWebSocketClient client = new GameWebSocketClient(listener);

        GameBoard board = new GameBoard();

        Tile original = new Tile(List.of(Direction.RIGHT, Direction.DOWN), true, null, null);

        setPrivateField(board, "rows", 1);
        setPrivateField(board, "cols", 1);
        setPrivateField(board, "tiles", List.of(List.of(original)));

        Method m = GameWebSocketClient.class.getDeclaredMethod("buildTiles", GameBoard.class);
        m.setAccessible(true);

        Tile[][] result = (Tile[][]) m.invoke(client, board);

        assertEquals(1, result.length);
        assertEquals(1, result[0].length);

        Tile copied = result[0][0];
        assertNotNull(copied);

        assertEquals(original.getEntrances(), copied.getEntrances());
        assertEquals(original.isFixed(), copied.isFixed());
        assertEquals(original.getTreasure(), copied.getTreasure());
        assertEquals(original.getBonus(), copied.getBonus());

        assertNotSame(original, copied);
    }
}
