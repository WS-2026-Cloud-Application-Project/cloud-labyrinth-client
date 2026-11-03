package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.domain.enums.AchievementType;
import com.uni.labyrinth.domain.enums.CommandType;
import com.uni.labyrinth.domain.enums.EventType;

public class AchievementUnlockedPayload {
    private final EventType type;
    private String playerId;
    private AchievementType achievement;


    public AchievementUnlockedPayload(String playerId, AchievementType achievement) {
        this.type = EventType.ACHIEVEMENT_UNLOCKED;
        this.playerId = playerId;
        this.achievement = achievement;
    }

    public EventType getType() {
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public AchievementType getAchievement() {
        return achievement;
    }
}
