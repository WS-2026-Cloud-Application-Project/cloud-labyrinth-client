package com.uni.labyrinth.service;

import com.uni.labyrinth.domain.enums.Direction;
import com.uni.labyrinth.model.*;

import java.util.*;

/**
 * Simuliert Board-Änderungen GENAU wie der Server
 */
public class BoardSimulator {
    
    /**
     * Simuliert einen Push genau wie der Server (GameBoard.pushTile)
     */
    public Tile[][] simulatePush(Tile[][] originalBoard, Tile extraTile,
                                 int rowOrColIndex, Direction direction, int rotations) {
        int rows = originalBoard.length;
        int cols = originalBoard[0].length;
        
        // Kopiere Board
        Tile[][] board = copyBoard(originalBoard);
        
        // Rotiere Extra-Tile
        Tile rotatedExtra = rotateExtra(extraTile, rotations);
        
        Tile tileToBePushedOut = null;
        
        // Hole Tile das rausgeschoben wird
        switch (direction) {
            case UP:
                tileToBePushedOut = board[0][rowOrColIndex];
                // Schiebe alle Tiles nach oben
                for (int r = 0; r < rows - 1; r++) {
                    board[r][rowOrColIndex] = board[r + 1][rowOrColIndex];
                }
                // Extra-Tile kommt unten rein
                board[rows - 1][rowOrColIndex] = rotatedExtra;
                break;
                
            case DOWN:
                tileToBePushedOut = board[rows - 1][rowOrColIndex];
                // Schiebe alle Tiles nach unten
                for (int r = rows - 1; r > 0; r--) {
                    board[r][rowOrColIndex] = board[r - 1][rowOrColIndex];
                }
                // Extra-Tile kommt oben rein
                board[0][rowOrColIndex] = rotatedExtra;
                break;
                
            case LEFT:
                tileToBePushedOut = board[rowOrColIndex][0];
                // Schiebe alle Tiles nach links
                for (int c = 0; c < cols - 1; c++) {
                    board[rowOrColIndex][c] = board[rowOrColIndex][c + 1];
                }
                // Extra-Tile kommt rechts rein
                board[rowOrColIndex][cols - 1] = rotatedExtra;
                break;
                
            case RIGHT:
                tileToBePushedOut = board[rowOrColIndex][cols - 1];
                // Schiebe alle Tiles nach rechts
                for (int c = cols - 1; c > 0; c--) {
                    board[rowOrColIndex][c] = board[rowOrColIndex][c - 1];
                }
                // Extra-Tile kommt links rein
                board[rowOrColIndex][0] = rotatedExtra;
                break;
        }
        
        return board;
    }
    
    /**
     * Rotiert ein Tile (rotiert die Entrances)
     */
    private Tile rotateExtra(Tile tile, int times) {
        if (tile == null || times == 0) {
            return tile;
        }
        
        List<Direction> rotatedEntrances = new ArrayList<>(tile.getEntrances());
        
        for (int i = 0; i < times; i++) {
            List<Direction> newEntrances = new ArrayList<>();
            for (Direction dir : rotatedEntrances) {
                newEntrances.add(rotateDirection(dir));
            }
            rotatedEntrances = newEntrances;
        }
        
        // Erstelle neues Tile mit rotierten Entrances
        return new Tile(rotatedEntrances, tile.isFixed(), tile.getTreasure(), tile.getBonus());
    }
    
    /**
     * Rotiert eine Richtung um 90° im Uhrzeigersinn
     */
    private Direction rotateDirection(Direction dir) {
        return switch (dir) {
            case UP -> Direction.RIGHT;
            case RIGHT -> Direction.DOWN;
            case DOWN -> Direction.LEFT;
            case LEFT -> Direction.UP;
        };
    }
    
    /**
     * Kopiert ein Board
     */
    private Tile[][] copyBoard(Tile[][] original) {
        int rows = original.length;
        int cols = original[0].length;
        Tile[][] copy = new Tile[rows][cols];
        
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                copy[r][c] = original[r][c];
            }
        }
        
        return copy;
    }
}
