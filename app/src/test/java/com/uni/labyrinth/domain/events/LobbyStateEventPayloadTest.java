package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.model.PlayerInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LobbyStateEventPayloadTest {

    @Test
    void getType() throws Exception {
        LobbyStateEventPayload payload = new LobbyStateEventPayload();
        setPrivateField(payload, "type", "LOBBY_STATE");

        assertEquals("LOBBY_STATE", payload.getType());
    }

    @Test
    void getPlayers() throws Exception {
        LobbyStateEventPayload payload = new LobbyStateEventPayload();

        PlayerInfo p1 = new PlayerInfo("1", "Andre", "RED", true, false, true);
        PlayerInfo p2 = new PlayerInfo("2", "Max", "BLUE", false, false, true);

        List<PlayerInfo> players = List.of(p1, p2);
        setPrivateField(payload, "players", players);

        assertEquals(players, payload.getPlayers());
        assertEquals(2, payload.getPlayers().size());
        assertEquals("Andre", payload.getPlayers().get(0).getName());
        assertTrue(payload.getPlayers().get(0).getIsAdmin());
    }

    @Test
    void getAdditionalProperties_and_setAdditionalProperties() {
        LobbyStateEventPayload payload = new LobbyStateEventPayload();

        Map<String, Object> props = Map.of("key", "value");
        payload.setAdditionalProperties(props);

        assertEquals(props, payload.getAdditionalProperties());
    }

    @Test
    void equals_and_hashCode() throws Exception {
        LobbyStateEventPayload a = new LobbyStateEventPayload();
        LobbyStateEventPayload b = new LobbyStateEventPayload();

        setPrivateField(a, "type", "LOBBY_STATE");
        setPrivateField(b, "type", "LOBBY_STATE");

        PlayerInfo p1 = new PlayerInfo("1", "Andre", "RED", true, false, true);
        List<PlayerInfo> players = List.of(p1);

        setPrivateField(a, "players", players);
        setPrivateField(b, "players", players);

        Map<String, Object> props = Map.of("x", 123);
        a.setAdditionalProperties(props);
        b.setAdditionalProperties(props);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_shouldContainClassName() throws Exception {
        LobbyStateEventPayload payload = new LobbyStateEventPayload();
        setPrivateField(payload, "type", "LOBBY_STATE");

        String text = payload.toString();

        assertNotNull(text);
        assertTrue(text.contains("LobbyStateEventPayload"));
        assertTrue(text.contains("type"));
    }

    // ---- helper (Reflection) ----
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
