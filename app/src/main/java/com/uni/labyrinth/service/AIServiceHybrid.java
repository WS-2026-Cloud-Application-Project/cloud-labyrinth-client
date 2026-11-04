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

/**
 * Hybrid AI Service - Kombiniert Client-Berechnung mit AI-Entscheidung
 * DEUTLICH intelligenter als die vorherige Version!
 */
public class AIServiceHybrid {

    private final OpenAiService openAiService;
    private final MoveSimulator simulator;
    private final Gson gson = new Gson();

    public AIServiceHybrid(String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
        this.simulator = new MoveSimulator();
    }

    public AIDecision getNextMove(GameState gameState) {
        // 1. Client berechnet die besten Optionen
        List<MoveSimulator.MoveOption> bestOptions = simulator.findBestMoves(gameState, 5);

        if (bestOptions.isEmpty()) {
            // Fallback: Bleibe stehen
            return createFallbackDecision(gameState);
        }

        // 2. Baue einfaches Prompt mit Optionen
        String prompt = buildPromptWithOptions(gameState, bestOptions);

        // 3. AI wählt beste Option
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", getSystemPrompt()));
        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(AIConfig.getModel())
                .messages(messages)
                .temperature(AIConfig.getTemperature())
                .maxTokens(AIConfig.getMaxTokens())
                .build();

        String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();

        return parseAIResponse(response, bestOptions);
    }

    private String getSystemPrompt() {
        return """
            Du bist eine KI für das Labyrinth-Spiel.

            Deine Aufgabe ist EINFACH:
            - Dir werden 3-5 vorberechnete Optionen gegeben
            - Jede Option zeigt: Push-Aktion, Zielposition, Distanz zum Schatz
            - Du wählst die BESTE Option aus

            ENTSCHEIDUNGS-KRITERIEN:
            1. Kleinste Distanz zum Schatz = BESTE Option
            2. Falls mehrere gleich gut: Wähle Option mit weniger Rotationen
            3. Nur falls Schatz nicht erreichbar: Bleibe in sicherer Position

            Antworte im JSON-Format:
            {
              "chosenOption": <Nummer der gewählten Option (1-5)>,
              "reasoning": "<Kurze Begründung (1 Satz)>"
            }

            BEISPIEL:
            {
              "chosenOption": 2,
              "reasoning": "Option 2 hat die kleinste Distanz zum Schatz (3 Felder)."
            }
            """;
    }

    private String buildPromptWithOptions(GameState gameState, List<MoveSimulator.MoveOption> options) {
        Coordinates myPos = gameState.getMyPlayerState().getCurrentPosition();
        Treasure treasure = gameState.getMyPlayerState().getCurrentTreasure();

        StringBuilder prompt = new StringBuilder();
        prompt.append("🎮 LABYRINTH SPIEL - WÄHLE BESTE OPTION\n\n");
        prompt.append("Deine Position: (").append(myPos.getColumn())
              .append(",").append(myPos.getRow()).append(")\n");
        prompt.append("Ziel: Schatz #").append(treasure != null ? treasure.getId() : "?").append("\n\n");

        prompt.append("VERFÜGBARE OPTIONEN (alle sind GÜLTIG):\n\n");

        for (int i = 0; i < options.size(); i++) {
            MoveSimulator.MoveOption opt = options.get(i);
            prompt.append("Option ").append(i + 1).append(":\n");
            prompt.append("  - Rotiere Extra-Tile ").append(opt.rotations).append("x\n");
            prompt.append("  - Schiebe Index ").append(opt.pushIndex)
                  .append(" in Richtung ").append(opt.pushDirection).append("\n");
            prompt.append("  - Gehe zu Position (").append(opt.targetPosition.column)
                  .append(",").append(opt.targetPosition.row).append(")\n");
            prompt.append("  - Distanz zum Schatz: ").append(opt.distanceToTreasure);

            if (i == 0) {
                prompt.append(" ⭐ BESTE DISTANZ");
            }
            prompt.append("\n\n");
        }

        prompt.append("Welche Option wählst du? (Wähle die mit der kleinsten Distanz!)");

        return prompt.toString();
    }

    private AIDecision parseAIResponse(String response, List<MoveSimulator.MoveOption> options) {
        try {
            // Extrahiere JSON
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;

            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                // Fallback: Wähle erste (beste) Option
                return convertOptionToDecision(options.get(0));
            }

            String jsonStr = response.substring(jsonStart, jsonEnd);
            JsonObject json = gson.fromJson(jsonStr, JsonObject.class);

            int chosenOption = json.get("chosenOption").getAsInt();
            String reasoning = json.get("reasoning").getAsString();

            // Validiere Wahl
            if (chosenOption < 1 || chosenOption > options.size()) {
                chosenOption = 1; // Fallback zur besten Option
            }

            MoveSimulator.MoveOption chosen = options.get(chosenOption - 1);
            AIDecision decision = convertOptionToDecision(chosen);
            decision.setReasoning(reasoning);

            return decision;

        } catch (Exception e) {
            System.err.println("Fehler beim Parsen: " + e.getMessage());
            // Fallback: Erste Option
            return convertOptionToDecision(options.get(0));
        }
    }

    private AIDecision convertOptionToDecision(MoveSimulator.MoveOption option) {
        AIDecision decision = new AIDecision();
        decision.setRotations(option.rotations);
        decision.setPushRowOrCol(option.pushIndex);
        decision.setPushDirection(option.pushDirection);
        decision.setMoveTarget(option.targetPosition);
        decision.setReasoning("Berechnete beste Option");
        return decision;
    }

    private AIDecision createFallbackDecision(GameState gameState) {
        Coordinates myPos = gameState.getMyPlayerState().getCurrentPosition();

        AIDecision decision = new AIDecision();
        decision.setRotations(0);
        decision.setPushRowOrCol(1); // Sicherer Index
        decision.setPushDirection(Direction.DOWN);
        decision.setMoveTarget(new Coordinates(myPos.getColumn(), myPos.getRow()));
        decision.setReasoning("Fallback: Keine gültigen Optionen gefunden");
        return decision;
    }
}
