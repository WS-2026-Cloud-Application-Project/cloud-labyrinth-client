package com.uni.labyrinth.service;

import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.*;

import java.util.*;

/**
 * Pathfinding-Service der genau wie der Server BFS verwendet
 * um alle erreichbaren Felder zu finden
 */
public class PathfindingService {
    
    private static final Map<Direction, Coordinates> DIRECTION_OFFSETS = new HashMap<>();
    
    static {
        DIRECTION_OFFSETS.put(Direction.UP, new Coordinates(0, -1));
        DIRECTION_OFFSETS.put(Direction.DOWN, new Coordinates(0, 1));
        DIRECTION_OFFSETS.put(Direction.LEFT, new Coordinates(-1, 0));
        DIRECTION_OFFSETS.put(Direction.RIGHT, new Coordinates(1, 0));
    }
    
    /**
     * Findet alle erreichbaren Felder von einer Startposition
     * Verwendet BFS genau wie der Server (GameManager.canPlayerMove)
     * @param occupiedPositions Positionen anderer Spieler (dürfen nicht betreten werden)
     */
    public Set<Coordinates> findReachableFields(Tile[][] board, Coordinates start, Set<Coordinates> occupiedPositions) {
        Set<Coordinates> reachable = new HashSet<>();
        
        if (start == null || board == null || board.length == 0) {
            return reachable;
        }
        
        if (occupiedPositions == null) {
            occupiedPositions = new HashSet<>();
        }
        
        int boardRows = board.length;
        int boardCols = board[0].length;
        
        // Startposition ist immer erreichbar
        reachable.add(new Coordinates(start.column, start.row));
        
        boolean[][] visited = new boolean[boardRows][boardCols];
        Queue<Coordinates> queue = new LinkedList<>();
        queue.add(start);
        visited[start.row][start.column] = true;
        
        while (!queue.isEmpty()) {
            Coordinates current = queue.poll();
            int currentColumn = current.column;
            int currentRow = current.row;
            
            Tile tile = getTile(board, current);
            if (tile == null) {
                continue;
            }
            
            // Prüfe alle Ausgänge des aktuellen Tiles
            for (Direction dir : tile.getEntrances()) {
                Coordinates offset = DIRECTION_OFFSETS.get(dir);
                int newColumn = currentColumn + offset.column;
                int newRow = currentRow + offset.row;
                
                // Außerhalb des Boards?
                if (newColumn < 0 || newColumn >= boardCols || newRow < 0 || newRow >= boardRows) {
                    continue;
                }
                
                Coordinates neighbor = new Coordinates(newColumn, newRow);
                
                // WICHTIG: Prüfe ob auf diesem Feld bereits ein anderer Spieler steht!
                if (isOccupied(neighbor, occupiedPositions)) {
                    continue;
                }
                
                Tile neighborTile = getTile(board, neighbor);
                
                if (neighborTile == null || visited[newRow][newColumn]) {
                    continue;
                }
                
                // Prüfe ob Nachbar-Tile entgegengesetzte Richtung hat
                boolean hasOppositeEntrance = false;
                for (Direction neighborDir : neighborTile.getEntrances()) {
                    if (isOppositeDirection(dir, neighborDir)) {
                        hasOppositeEntrance = true;
                        break;
                    }
                }
                
                if (!hasOppositeEntrance) {
                    continue;
                }
                
                // Feld ist erreichbar!
                reachable.add(neighbor);
                queue.add(neighbor);
                visited[newRow][newColumn] = true;
            }
        }
        
        return reachable;
    }
    
    /**
     * Berechnet die Manhattan-Distanz zwischen zwei Koordinaten
     */
    public int calculateDistance(Coordinates from, Coordinates to) {
        return Math.abs(from.column - to.column) + Math.abs(from.row - to.row);
    }
    
    /**
     * Prüft ob zwei Richtungen entgegengesetzt sind (wie im Server)
     */
    private boolean isOppositeDirection(Direction dir1, Direction dir2) {
        return (dir1.equals(Direction.UP) && dir2.equals(Direction.DOWN)) ||
               (dir1.equals(Direction.DOWN) && dir2.equals(Direction.UP)) ||
               (dir1.equals(Direction.LEFT) && dir2.equals(Direction.RIGHT)) ||
               (dir1.equals(Direction.RIGHT) && dir2.equals(Direction.LEFT));
    }
    
    /**
     * Prüft ob ein Feld von einem anderen Spieler besetzt ist
     */
    private boolean isOccupied(Coordinates position, Set<Coordinates> occupiedPositions) {
        for (Coordinates occupied : occupiedPositions) {
            if (occupied.column == position.column && occupied.row == position.row) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Holt ein Tile an gegebener Position
     */
    private Tile getTile(Tile[][] board, Coordinates coords) {
        if (coords.row >= 0 && coords.row < board.length &&
            coords.column >= 0 && coords.column < board[0].length) {
            return board[coords.row][coords.column];
        }
        return null;
    }
}
