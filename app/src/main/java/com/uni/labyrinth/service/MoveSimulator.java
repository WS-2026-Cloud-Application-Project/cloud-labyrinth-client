package com.uni.labyrinth.service;

import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.*;

import java.util.*;

/**
 * Simuliert verschiedene Spielzüge und findet die besten Optionen
 */
public class MoveSimulator {
    
    private final PathfindingService pathfinding;
    private final BoardSimulator boardSimulator;
    
    public MoveSimulator() {
        this.pathfinding = new PathfindingService();
        this.boardSimulator = new BoardSimulator();
    }
    
    /**
     * Repräsentiert eine mögliche Spiel-Option
     */
    public static class MoveOption {
        public int rotations;
        public int pushIndex;
        public Direction pushDirection;
        public Coordinates targetPosition;
        public int distanceToTreasure;
        public Set<Coordinates> reachableFields;
        
        @Override
        public String toString() {
            return String.format("Push %d %s, rotate %dx → go to (%d,%d), distance: %d", 
                pushIndex, pushDirection, rotations, 
                targetPosition.column, targetPosition.row, distanceToTreasure);
        }
    }
    
    /**
     * Findet die besten Züge durch Simulation
     */
    public List<MoveOption> findBestMoves(GameState gameState, int maxOptions) {
        Tile[][] board = gameState.getBoard();
        Coordinates myPos = gameState.getMyPlayerState().getCurrentPosition();
        Coordinates currentPos = new Coordinates(myPos.getColumn(), myPos.getRow());
        Treasure currentTreasure = gameState.getMyPlayerState().getCurrentTreasure();
        Tile extraTile = gameState.getExtraTile();
        LastPush lastPush = gameState.getLastPush();
        
        // Sammle Positionen anderer Spieler (WICHTIG: Kollisions-Regel!)
        Set<Coordinates> occupiedPositions = getOtherPlayerPositions(gameState);
        
        // Finde Schatzposition
        Coordinates treasurePos = findTreasurePosition(board, currentTreasure != null ? currentTreasure.getId() : -1);
        
        List<MoveOption> allOptions = new ArrayList<>();
        
        // Teste alle möglichen Push-Aktionen
        int boardSize = board.length;
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 1; i < boardSize; i += 2) {
            validIndices.add(i);
        }
        
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        
        System.out.println(" Simuliere Züge... LastPush=" + (lastPush != null ? "Index=" + lastPush.getRowOrColumnIndex() + ", Dir=" + lastPush.getDirection() : "null"));
        int validCount = 0;
        int filteredCount = 0;
        
        for (int index : validIndices) {
            for (Direction dir : directions) {
                // Überspringe verbotene Push-Aktion (würde letzten Zug rückgängig machen)
                if (isInvalidPush(index, dir, lastPush)) {
                    filteredCount++;
                    continue;
                }
                validCount++;
                
                // Teste verschiedene Rotationen (0-2)
                for (int rotations = 0; rotations <= 2; rotations++) {
                    // ECHTE Simulation wie der Server!
                    Tile[][] simulatedBoard = boardSimulator.simulatePush(board, extraTile, index, dir, rotations);
                    
                    // TODO: Spieler-Position könnte sich durch Push ändern (wenn auf derselben Reihe/Spalte)
                    // Für jetzt: Verwende aktuelle Position
                    
                    // Finde erreichbare Felder nach dem Push (mit Kollisions-Prüfung!)
                    Set<Coordinates> reachable = pathfinding.findReachableFields(simulatedBoard, currentPos, occupiedPositions);
                    
                    // Finde beste Zielposition (nächste zum Schatz)
                    Coordinates bestTarget = currentPos; // Fallback: bleibe stehen
                    int bestDistance = Integer.MAX_VALUE;
                    
                    if (treasurePos != null) {
                        for (Coordinates reachablePos : reachable) {
                            int distance = pathfinding.calculateDistance(reachablePos, treasurePos);
                            if (distance < bestDistance) {
                                bestDistance = distance;
                                bestTarget = reachablePos;
                            }
                        }
                    } else {
                        // Schatz nicht auf Board - bleibe einfach stehen
                        bestDistance = 999;
                    }
                    
                    MoveOption option = new MoveOption();
                    option.rotations = rotations;
                    option.pushIndex = index;
                    option.pushDirection = dir;
                    option.targetPosition = bestTarget;
                    option.distanceToTreasure = bestDistance;
                    option.reachableFields = reachable;
                    
                    allOptions.add(option);
                }
            }
        }
        
        System.out.println(" Simulation komplett: " + validCount + " gültige Optionen, " + filteredCount + " Push-Backs gefiltert");
        
        // Sortiere nach Distanz zum Schatz (beste zuerst)
        allOptions.sort(Comparator.comparingInt(o -> o.distanceToTreasure));
        
        if (!allOptions.isEmpty()) {
            System.out.println(" Beste Option: " + allOptions.get(0).toString());
        }
        
        // Gebe Top N Optionen zurück
        return allOptions.subList(0, Math.min(maxOptions, allOptions.size()));
    }

    
    /**
     * Prüft ob ein Push ungültig ist (würde letzten Zug rückgängig machen)
     */
    private boolean isInvalidPush(int index, Direction direction, LastPush lastPush) {
        if (lastPush == null) {
            return false;
        }

        int lastIndex = lastPush.getRowOrColumnIndex();
        Direction lastDirection = lastPush.getDirection();

        // Anderer Index → OK
        if (lastIndex != index) {
            return false;
        }

        // Gleicher Index - prüfe ob entgegengesetzte Richtung (PUSH-BACK!)
        boolean isOpposite = (lastDirection == Direction.UP && direction == Direction.DOWN) ||
                            (lastDirection == Direction.DOWN && direction == Direction.UP) ||
                            (lastDirection == Direction.LEFT && direction == Direction.RIGHT) ||
                            (lastDirection == Direction.RIGHT && direction == Direction.LEFT);

        return isOpposite;
    }
    
    /**
     * Sammelt Positionen aller anderen Spieler (für Kollisions-Prüfung)
     */
    private Set<Coordinates> getOtherPlayerPositions(GameState gameState) {
        Set<Coordinates> occupied = new HashSet<>();
        String myPlayerId = gameState.getMyPlayerState().getPlayerInfo().getId();
        
        for (PlayerState player : gameState.getPlayers()) {
            if (player.getPlayerInfo() != null &&
                !player.getPlayerInfo().getId().equals(myPlayerId)) {
                Coordinates pos = player.getCurrentPosition();
                if (pos != null) {
                    occupied.add(new Coordinates(pos.getColumn(), pos.getRow()));
                }
            }
        }
        
        return occupied;
    }
    
    /**
     * Findet die Position eines Schatzes
     */
    private Coordinates findTreasurePosition(Tile[][] board, int treasureId) {
        if (treasureId < 0) return null;
        
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                Tile tile = board[row][col];
                if (tile != null && tile.getTreasure() != null && 
                    tile.getTreasure().getId() == treasureId) {
                    return new Coordinates(col, row);
                }
            }
        }
        return null;
    }
}
