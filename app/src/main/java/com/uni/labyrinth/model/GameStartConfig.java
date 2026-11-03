package com.uni.labyrinth.model;

public record GameStartConfig(
        int boardSize,
        int treasureCountPerPlayer,
        int gameDurationInSeconds,
        int bonusCount
) {}
