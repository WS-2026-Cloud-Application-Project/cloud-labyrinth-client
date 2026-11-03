package com.uni.labyrinth.model;

import java.util.Map;
import java.util.Objects;

public class RankingEntry {
    private String playerId;
    private int rank;
    private int score;
    private PlayerGameStats stats;
    private Map<String, Object> additionalProperties;

    public RankingEntry() {
    }

    public RankingEntry(String playerId, int rank, int score, PlayerGameStats stats) {
        this.playerId = playerId;
        this.rank = rank;
        this.score = score;
        this.stats = stats;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public PlayerGameStats getStats() {
        return stats;
    }

    public void setStats(PlayerGameStats stats) {
        this.stats = stats;
    }

    public Map<String, Object> getAdditionalProperties() { return this.additionalProperties; }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) { this.additionalProperties = additionalProperties; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RankingEntry self = (RankingEntry) o;
        return
                Objects.equals(this.playerId, self.playerId) &&
                        Objects.equals(this.rank, self.rank) &&
                        Objects.equals(this.score, self.score) &&
                        Objects.equals(this.stats, self.stats) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object)playerId, (Object)rank, (Object)score, (Object)stats, (Object)additionalProperties);
    }

    @Override
    public String toString() {
        return "class RankingEntry {\n" +
                "    playerId: " + toIndentedString(playerId) + "\n" +
                "    rank: " + toIndentedString(rank) + "\n" +
                "    score: " + toIndentedString(score) + "\n" +
                "    stats: " + toIndentedString(stats) + "\n" +
                "    additionalProperties: " + toIndentedString(additionalProperties) + "\n" +
                "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
