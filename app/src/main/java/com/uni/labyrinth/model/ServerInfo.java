package com.uni.labyrinth.model;


import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;


public class ServerInfo {
    private String type;
    private OffsetDateTime serverTime;
    private String serverVersion;
    private String protocolVersion;
    private String motd;
    private Duration timeOffset;
    private Map<String, Object> additionalProperties;
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }




    public OffsetDateTime getServerTime() {
        return this.serverTime;
    }

    public void setServerTime(OffsetDateTime serverTime) {
        this.serverTime = serverTime;
        this.timeOffset = Duration.between(Instant.now(),serverTime);
    }

    public Instant getServerAdjustedTime(Instant serverTime) {
        return serverTime.plus(timeOffset);
    }


    public String getServerVersion() {
        return this.serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }



}