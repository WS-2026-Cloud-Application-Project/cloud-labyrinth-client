package com.uni.labyrinth.domain.commands;

import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.model.BoardSize;

public class StartGameCommandPayload {

    private CommandType type;
    private BoardSize boardSize;
    private int treasureCardCount;
    private int totalBonusCount;
    private int gameDurationInSeconds;

    public StartGameCommandPayload() {}

    public StartGameCommandPayload(
            CommandType type,
            BoardSize boardSize,
            int treasureCardCount,
            int gameDurationInSeconds,
            int totalBonusCount
    ) {
        this.type = type;
        this.boardSize = boardSize;
        this.treasureCardCount = treasureCardCount;
        this.totalBonusCount = totalBonusCount;
        this.gameDurationInSeconds = gameDurationInSeconds;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public BoardSize getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(BoardSize boardSize) {
        this.boardSize = boardSize;
    }

    public int getTreasureCardCount() {
        return treasureCardCount;
    }

    public void setTreasureCardCount(int treasureCardCount) {
        this.treasureCardCount = treasureCardCount;
    }

    public int getTotalBonusCount() {
        return totalBonusCount;
    }

    public void setTotalBonusCount(int totalBonusCount) {
        this.totalBonusCount = totalBonusCount;
    }

    public int getGameDurationInSeconds() {
        return gameDurationInSeconds;
    }

    public void setGameDurationInSeconds(int gameDurationInSeconds) {
        this.gameDurationInSeconds = gameDurationInSeconds;
    }
}
