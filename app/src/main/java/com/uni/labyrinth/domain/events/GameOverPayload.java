package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.model.RankingEntry;

import java.util.List;

public class GameOverPayload {
    private String type;
    private String winnerId;
    private List<RankingEntry> ranking;

    public GameOverPayload() {
    }

    public GameOverPayload(String winnerId, List<RankingEntry> ranking) {
        this.type = "GAME_OVER";
        this.winnerId = winnerId;
        this.ranking = ranking;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public List<RankingEntry> getRanking() {
        return ranking;
    }

    public void setRanking(List<RankingEntry> ranking) {
        this.ranking = ranking;
    }
}
