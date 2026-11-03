package com.uni.labyrinth.service;

import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.*;

import java.util.*;

/**
 * Sichere Zug-Strategie: Keine Simulation, nur garantiert gültige Züge
 */
public class SafeMoveStrategy {
    
    private final PathfindingService pathfinding;
    
    public SafeMoveStrategy() {
        this.pathfinding = new PathfindingService();
    }
    
    public static class SafeMoveOption {
        public Coordinates targetPosition;
        public int distanceToTarget;  // Distanz zu Schatz ODER Home
        public boolean isCurrentPosition;
        public boolean isGoingHome;   // True wenn Ziel die Heimatposition ist

        // Getter für Kompatibilität
        public int getDistanceToTreasure() {
            return distanceToTarget;
        }

        @Override
        public String toString() {
            String targetType = isGoingHome ? "HOME" : "SCHATZ";
            return String.format("Go to (%d,%d), distance to %s: %d%s",
                targetPosition.column, targetPosition.row, targetType, distanceToTarget,
                isCurrentPosition ? " (STAY)" : "");
        }
    }

    // Ergebnis-Klasse für Zielermittlung
    public static class TargetInfo {
        public Coordinates position;
        public boolean isHome;

        public TargetInfo(Coordinates position, boolean isHome) {
            this.position = position;
            this.isHome = isHome;
        }
    }
    
    /**
     * Ermittelt das aktuelle Ziel: Schatz oder Heimatposition
     */
    public TargetInfo determineTarget(GameState gameState) {
        PlayerState myPlayer = gameState.getMyPlayerState();
        Tile[][] board = gameState.getBoard();

        // WICHTIG: Prüfe ob alle Schätze gesammelt wurden
        // Wenn remainingTreasureCount == 0, dann SOFORT nach Hause!
        // (egal ob currentTreasure noch gesetzt ist oder nicht)
        if (myPlayer.getRemainingTreasureCount() == 0) {
            // Alle Schätze gesammelt → Ab nach Hause!
            Coordinates home = myPlayer.getHomePosition();
            System.out.println(" AI-ZIEL: ALLE SCHÄTZE GESAMMELT! Gehe nach Hause um zu GEWINNEN!");
            System.out.println("       Heimatposition: (" + home.getColumn() + "," + home.getRow() + ")");
            System.out.println("       RemainingTreasureCount: " + myPlayer.getRemainingTreasureCount());
            return new TargetInfo(new Coordinates(home.getColumn(), home.getRow()), true);
        }

        // Noch Schätze zu sammeln
        Treasure currentTreasure = myPlayer.getCurrentTreasure();
        if (currentTreasure != null) {
            Coordinates treasurePos = findTreasurePosition(board, currentTreasure.getId());
            System.out.println(" AI-ZIEL: Suche Schatz #" + currentTreasure.getId());
            System.out.println("   Verbleibende Schätze: " + myPlayer.getRemainingTreasureCount());
            return new TargetInfo(treasurePos, false);
        }

        // Kein Schatz und keine Home-Bedingung → Fallback (sollte nie passieren)
        System.out.println(" WARNUNG: Kein Ziel gefunden! RemainingCount=" + myPlayer.getRemainingTreasureCount());
        return new TargetInfo(null, false);
    }

    /**
     * Findet AKTUELL erreichbare Ziele (ohne Simulation, 100% sicher!)
     */
    public List<SafeMoveOption> findSafeMoves(GameState gameState) {
        Tile[][] board = gameState.getBoard();
        Coordinates myPos = gameState.getMyPlayerState().getCurrentPosition();
        Coordinates currentPos = new Coordinates(myPos.getColumn(), myPos.getRow());

        // Sammle besetzte Positionen
        Set<Coordinates> occupiedPositions = getOtherPlayerPositions(gameState);

        // Finde AKTUELL erreichbare Felder (ohne Push-Simulation!)
        Set<Coordinates> reachable = pathfinding.findReachableFields(board, currentPos, occupiedPositions);

        // Ermittle Ziel (Schatz oder Home)
        TargetInfo target = determineTarget(gameState);

        List<SafeMoveOption> options = new ArrayList<>();

        for (Coordinates reachablePos : reachable) {
            SafeMoveOption option = new SafeMoveOption();
            option.targetPosition = reachablePos;
            option.isCurrentPosition = (reachablePos.column == currentPos.column &&
                                       reachablePos.row == currentPos.row);
            option.isGoingHome = target.isHome;

            if (target.position != null) {
                option.distanceToTarget = pathfinding.calculateDistance(reachablePos, target.position);
            } else {
                option.distanceToTarget = 999;
            }

            options.add(option);
        }

        // Sortiere: Nächste zum Ziel zuerst
        options.sort(Comparator.comparingInt(o -> o.distanceToTarget));

        // WICHTIG: Entferne die aktuelle Position, WENN es mindestens eine andere Option gibt
        // Die AI soll sich immer bewegen, außer wenn es absolut keine andere Wahl gibt!
        if (options.size() > 1) {
            List<SafeMoveOption> movingOptions = new ArrayList<>();
            for (SafeMoveOption option : options) {
                if (!option.isCurrentPosition) {
                    movingOptions.add(option);
                }
            }

            // Nur wenn es wirklich Bewegungsoptionen gibt, verwende diese
            if (!movingOptions.isEmpty()) {
                System.out.println(" Filter aktuelle Position heraus - AI bewegt sich!");
                System.out.println("   Optionen ohne Stillstand: " + movingOptions.size());
                return movingOptions;
            }
        }

        // Fallback: Nur wenn absolut keine Bewegung möglich ist, bleibe stehen
        if (options.size() == 1 && options.get(0).isCurrentPosition) {
            System.out.println("  Keine Bewegung möglich - AI bleibt stehen");
        }

        return options;
    }
    
    /**
     * Findet sicheren Push (vermeidet Push-Back)
     */
    public PushInfo findSafePush(GameState gameState) {
        LastPush lastPush = gameState.getLastPush();
        int boardSize = gameState.getBoard().length;

        System.out.println("\n === PUSH-BACK DETECTION ===");
        if (lastPush != null) {
            System.out.println("LastPush: Index=" + lastPush.getRowOrColumnIndex() + ", Direction=" + lastPush.getDirection());
        } else {
            System.out.println("LastPush: null (erster Zug)");
        }
        System.out.println("================================\n");

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 1; i < boardSize; i += 2) {
            validIndices.add(i);
        }

        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        // Sammle alle gültigen Optionen
        List<PushInfo> validPushes = new ArrayList<>();

        for (int index : validIndices) {
            for (Direction dir : directions) {
                if (!isInvalidPush(index, dir, lastPush)) {
                    validPushes.add(new PushInfo(index, dir));
                    System.out.println(" GÜLTIG: Index=" + index + ", Dir=" + dir);
                } else {
                    System.out.println(" VERBOTEN (Push-Back): Index=" + index + ", Dir=" + dir);
                }
            }
        }

        if (validPushes.isEmpty()) {
            System.out.println("  KRITISCHER FEHLER: Keine gültigen Pushes! Verwende Fallback");
            return new PushInfo(1, Direction.DOWN);
        }

        // Wähle zufällig einen gültigen Push
        PushInfo chosen = validPushes.get(new java.util.Random().nextInt(validPushes.size()));
        System.out.println(" Gewählter Push: Index=" + chosen.index + ", Dir=" + chosen.direction + "\n");

        return chosen;
    }
    
    public static class PushInfo {
        public int index;
        public Direction direction;
        
        public PushInfo(int index, Direction direction) {
            this.index = index;
            this.direction = direction;
        }
    }
    
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

        if (isOpposite) {
            System.out.println("    →   PUSH-BACK ERKANNT: Index=" + index + ", Last=" + lastDirection + ", Neu=" + direction);
        }

        return isOpposite;
    }
    
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
