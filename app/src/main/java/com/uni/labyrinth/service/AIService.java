package com.uni.labyrinth.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.*;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;

public class AIService {

    private final OpenAiService openAiService;
    private final Gson gson = new Gson();

    public AIService(String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    public AIDecision getNextMove(GameState gameState) {
        String prompt = buildPrompt(gameState);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", getSystemPrompt()));
        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .temperature(0.7)
                .maxTokens(500)
                .build();

        String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();

        return parseAIResponse(response);
    }

    private String getSystemPrompt() {
        return """
            Du bist ein KI-Spieler für das Labyrinth-Spiel.

            WICHTIGE SPIELREGELN:
            1. In jedem Zug musst du zuerst das Extra-Tile rotieren (0-3 mal um 90°)
            2. Dann musst du das Tile in eine Reihe/Spalte einschieben (NUR ungerade Indizes: 1, 3, 5, etc.)
            3. Danach bewegst du deine Spielfigur zu einem ERREICHBAREN Feld
            4. Ziel: Erreiche dein aktuelles Schatz-Feld (currentTreasure)

            KRITISCH - Bewegungs-Regeln:
            - Du MUSST versuchen, dich zum Schatz zu bewegen! Das ist das Hauptziel!
            - Du kannst NUR zu Feldern gehen, die über VERBUNDENE Pfade erreichbar sind
            - Ein Tile mit ["UP", "DOWN"] hat Ausgänge nach oben und unten
            - Du kannst nur von einem Feld zum nächsten, wenn BEIDE Tiles eine passende Verbindung haben
            - Beispiel: Von (0,0) nach (1,0) geht nur, wenn Tile (0,0) "RIGHT" hat UND Tile (1,0) "LEFT" hat
            - An deiner Position bleiben ist NUR erlaubt, wenn KEINE Bewegung möglich ist!

            Push-Richtungen (NUR ungerade rowOrColIndex erlaubt!):
            - UP: Schiebe Spalte von unten nach oben (rowOrColIndex = column)
            - DOWN: Schiebe Spalte von oben nach unten (rowOrColIndex = column)
            - LEFT: Schiebe Reihe von rechts nach links (rowOrColIndex = row)
            - RIGHT: Schiebe Reihe von links nach rechts (rowOrColIndex = row)

            WICHTIGE REGEL - Kein Zurückschieben:
            - Du darfst NICHT den letzten Push rückgängig machen!
            - Wenn z.B. gerade "Index 1, DOWN" geschoben wurde, darfst du NICHT "Index 1, UP" schieben
            - Wähle einen ANDEREN Index oder eine ANDERE Richtung!

            SPIEL-STRATEGIE:
            1. Rotiere das Extra-Tile so, dass es dir hilft (0-2 Rotationen)
            2. Schiebe eine UNGERADE Reihe/Spalte (1, 3, 5), um Wege zu öffnen
            3. BEWEGE DICH zum Schatz oder näher zum Schatz!
               - Prüfe welche Nachbarfelder (oben/unten/links/rechts) erreichbar sind
               - Wähle das Feld, das dich dem Schatz näher bringt
               - Wenn KEINE Bewegung möglich ist, bleibe stehen
            4. Sei mutig aber vorsichtig!

            Antworte IMMER im folgenden JSON-Format:
            {
              "rotations": <Anzahl der Rotationen 0-2>,
              "pushAction": {
                "rowOrColIndex": <UNGERADER Index: 1, 3, oder 5 - NICHT der verbotene!>,
                "direction": "<UP/DOWN/LEFT/RIGHT - NICHT die verbotene Richtung!>"
              },
              "moveAction": {
                "column": <NUR ein ✓ ERREICHBAR Feld ODER deine aktuelle column>,
                "row": <NUR ein ✓ ERREICHBAR Feld ODER deine aktuelle row>
              },
              "reasoning": "<Begründung: Welches ✓ ERREICHBAR Feld oder warum bleibst du stehen?>"
            }

            BEISPIEL wenn KEIN Feld erreichbar ist:
            {
              "rotations": 1,
              "pushAction": {"rowOrColIndex": 3, "direction": "RIGHT"},
              "moveAction": {"column": <DEINE_AKTUELLE_COLUMN>, "row": <DEINE_AKTUELLE_ROW>},
              "reasoning": "Kein Nachbarfeld ist ✓ ERREICHBAR, daher bleibe ich an meiner Position."
            }
            """;
    }

    private String buildPrompt(GameState gameState) {
        PlayerState myPlayer = gameState.getMyPlayerState();
        Treasure currentTreasure = myPlayer.getCurrentTreasure();
        Coordinates myPosition = myPlayer.getCurrentPosition();
        Tile[][] board = gameState.getBoard();
        Tile extraTile = gameState.getExtraTile();

        // Finde die Position des aktuellen Schatzes auf dem Board
        Coordinates treasurePos = findTreasurePosition(board, currentTreasure.getId());

        StringBuilder prompt = new StringBuilder();
        prompt.append("Aktuelle Spielsituation:\n\n");
        prompt.append("Deine Position: column=").append(myPosition.getColumn())
              .append(", row=").append(myPosition.getRow()).append("\n");

        if (treasurePos != null) {
            prompt.append("Ziel-Schatz Position: column=")
                  .append(treasurePos.column).append(", row=")
                  .append(treasurePos.row).append("\n");
        } else {
            prompt.append("Ziel-Schatz Position: UNBEKANNT (muss erst auf dem Board erscheinen)\n");
        }
        prompt.append("Schatz-ID: ").append(currentTreasure.getId()).append("\n\n");

        prompt.append("Extra-Tile Verbindungen: ").append(extraTile.getEntrances()).append("\n");
        prompt.append("Extra-Tile ist fixed: ").append(extraTile.isFixed()).append("\n\n");

        prompt.append("Brett-Größe: ").append(board.length).append(" x ").append(board[0].length).append("\n");
        prompt.append("Brett-Übersicht:\n");
        prompt.append(simplifyBoard(board, myPosition, treasurePos)).append("\n");

        prompt.append("\n=== WICHTIGE HINWEISE ===\n");
        prompt.append("Gültige Push-Indizes: ");
        for (int i = 1; i < board.length; i += 2) {
            prompt.append(i).append(i < board.length - 2 ? ", " : "");
        }
        prompt.append("\n");

        // Zeige die letzte Push-Aktion an (darf NICHT rückgängig gemacht werden!)
        LastPush lastPush = gameState.getLastPush();
        if (lastPush != null) {
            prompt.append("\n⚠️  WICHTIG - VERBOTENER ZUG:\n");
            prompt.append("Letzter Push war: Index=").append(lastPush.getRowOrColumnIndex())
                  .append(", Richtung=").append(lastPush.getDirection()).append("\n");
            prompt.append("Du DARFST NICHT den gleichen Index in die ENTGEGENGESETZTE Richtung schieben!\n");
            prompt.append("(Das würde den letzten Zug rückgängig machen - NICHT ERLAUBT!)\n");
        }
        prompt.append("Deine aktuelle Tile-Verbindungen: ").append(board[myPosition.getRow()][myPosition.getColumn()].getEntrances()).append("\n");

        // Füge Nachbar-Tiles Informationen hinzu
        prompt.append("\nNACHBAR-TILES (prüfe BEIDE Verbindungen!):\n");
        // Oben
        if (myPosition.getRow() > 0) {
            Tile topTile = board[myPosition.getRow() - 1][myPosition.getColumn()];
            prompt.append("- OBEN (").append(myPosition.getColumn()).append(",").append(myPosition.getRow() - 1)
                  .append("): ").append(topTile.getEntrances());
            boolean canGoUp = board[myPosition.getRow()][myPosition.getColumn()].getEntrances().contains(Direction.UP)
                           && topTile.getEntrances().contains(Direction.DOWN);
            prompt.append(canGoUp ? " ✓ ERREICHBAR\n" : " ✗ NICHT ERREICHBAR\n");
        }
        // Unten
        if (myPosition.getRow() < board.length - 1) {
            Tile bottomTile = board[myPosition.getRow() + 1][myPosition.getColumn()];
            prompt.append("- UNTEN (").append(myPosition.getColumn()).append(",").append(myPosition.getRow() + 1)
                  .append("): ").append(bottomTile.getEntrances());
            boolean canGoDown = board[myPosition.getRow()][myPosition.getColumn()].getEntrances().contains(Direction.DOWN)
                             && bottomTile.getEntrances().contains(Direction.UP);
            prompt.append(canGoDown ? " ✓ ERREICHBAR\n" : " ✗ NICHT ERREICHBAR\n");
        }
        // Links
        if (myPosition.getColumn() > 0) {
            Tile leftTile = board[myPosition.getRow()][myPosition.getColumn() - 1];
            prompt.append("- LINKS (").append(myPosition.getColumn() - 1).append(",").append(myPosition.getRow())
                  .append("): ").append(leftTile.getEntrances());
            boolean canGoLeft = board[myPosition.getRow()][myPosition.getColumn()].getEntrances().contains(Direction.LEFT)
                             && leftTile.getEntrances().contains(Direction.RIGHT);
            prompt.append(canGoLeft ? " ✓ ERREICHBAR\n" : " ✗ NICHT ERREICHBAR\n");
        }
        // Rechts
        if (myPosition.getColumn() < board[0].length - 1) {
            Tile rightTile = board[myPosition.getRow()][myPosition.getColumn() + 1];
            prompt.append("- RECHTS (").append(myPosition.getColumn() + 1).append(",").append(myPosition.getRow())
                  .append("): ").append(rightTile.getEntrances());
            boolean canGoRight = board[myPosition.getRow()][myPosition.getColumn()].getEntrances().contains(Direction.RIGHT)
                              && rightTile.getEntrances().contains(Direction.LEFT);
            prompt.append(canGoRight ? " ✓ ERREICHBAR\n" : " ✗ NICHT ERREICHBAR\n");
        }

        prompt.append("\n⚠️  KRITISCHE BEWEGUNGS-REGEL - LIES AUFMERKSAM:\n");
        prompt.append("Du MUSST die moveAction GENAU so wählen:\n");
        prompt.append("1. Schaue dir die NACHBAR-TILES Liste oben an\n");
        prompt.append("2. Gibt es mindestens EIN Feld mit ✓ ERREICHBAR?\n");
        prompt.append("   JA → Wähle dieses Feld (column, row)\n");
        prompt.append("   NEIN → Wähle DEINE AKTUELLE Position: column=").append(myPosition.getColumn())
              .append(", row=").append(myPosition.getRow()).append("\n");
        prompt.append("3. Wähle NIEMALS ein Feld mit ✗ NICHT ERREICHBAR!\n\n");

        prompt.append("DEINE AUFGABE:\n");
        prompt.append("1. Rotiere Extra-Tile (0-2 mal)\n");
        prompt.append("2. Schiebe einen ungeraden Index (z.B. 1 oder 3) - beachte verbotene Züge!\n");
        prompt.append("3. Für moveAction: Schaue in NACHBAR-TILES Liste nach ✓ ERREICHBAR\n");
        prompt.append("\nZIEL: Erreiche den Schatz bei ");
        if (treasurePos != null) {
            prompt.append("column=").append(treasurePos.column)
                  .append(", row=").append(treasurePos.row);
        } else {
            prompt.append("(noch nicht sichtbar)");
        }
        prompt.append("\n\nWelchen Zug möchtest du machen?");
        return prompt.toString();
    }

    private Coordinates findTreasurePosition(Tile[][] board, int treasureId) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                Tile tile = board[row][col];
                if (tile != null && tile.getTreasure() != null &&
                    tile.getTreasure().getId() == treasureId) {
                    return new Coordinates(col, row);
                }
            }
        }
        return null; // Schatz noch nicht auf dem Board
    }

    private String simplifyBoard(Tile[][] board, Coordinates myPos, Coordinates treasurePos) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (myPos.getRow() == row && myPos.getColumn() == col) {
                    sb.append(" P ");
                } else if (treasurePos != null && treasurePos.row == row && treasurePos.column == col) {
                    sb.append(" T ");
                } else {
                    Tile tile = board[row][col];
                    if (tile.isFixed()) {
                        sb.append(" F ");
                    } else {
                        sb.append(" . ");
                    }
                }
            }
            sb.append("\n");
        }
        sb.append("\nLegende: P=Du, T=Schatz, F=Fixed Tile, .=Bewegliches Tile\n");
        return sb.toString();
    }

    private AIDecision parseAIResponse(String response) {
        try {
            // Extrahiere JSON aus der Antwort (falls zusätzlicher Text vorhanden)
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;

            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw new RuntimeException("Keine gültige JSON-Antwort gefunden");
            }

            String jsonStr = response.substring(jsonStart, jsonEnd);
            JsonObject json = gson.fromJson(jsonStr, JsonObject.class);

            AIDecision decision = new AIDecision();
            decision.setRotations(json.get("rotations").getAsInt());

            JsonObject pushAction = json.getAsJsonObject("pushAction");
            decision.setPushRowOrCol(pushAction.get("rowOrColIndex").getAsInt());
            decision.setPushDirection(Direction.valueOf(pushAction.get("direction").getAsString()));

            JsonObject moveAction = json.getAsJsonObject("moveAction");
            decision.setMoveTarget(new Coordinates(
                moveAction.get("column").getAsInt(),
                moveAction.get("row").getAsInt()
            ));

            decision.setReasoning(json.get("reasoning").getAsString());

            return decision;
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Parsen der AI-Antwort: " + e.getMessage() +
                                     "\nAntwort war: " + response, e);
        }
    }
}
