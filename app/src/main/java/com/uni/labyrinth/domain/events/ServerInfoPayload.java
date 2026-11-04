package com.uni.labyrinth.domain.events;


import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;


public class ServerInfoPayload {
    private String type;
    private String serverTime;
    private String serverVersion;
    private String protocolVersion;
    private String motd;
    private Map<String, Object> additionalProperties;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServerTime() {
        return this.serverTime.toString();
    }

    public OffsetDateTime getServerTimeAsOffsetDateTime() {
        return  OffsetDateTime.parse(this.serverTime);
    }



    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerInfoPayload self = (ServerInfoPayload) o;
        return
                Objects.equals(this.type, self.type) &&
                        Objects.equals(this.serverTime, self.serverTime) &&
                        Objects.equals(this.serverVersion, self.serverVersion) &&
                        Objects.equals(this.protocolVersion, self.protocolVersion) &&
                        Objects.equals(this.motd, self.motd) &&
                        Objects.equals(this.additionalProperties, self.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) type, (Object) serverTime, (Object) serverVersion, (Object) protocolVersion, (Object) motd, (Object) additionalProperties);
    }

    @Override
    public String toString() {
        return "class ServerInfoPayload {\n" +
                "    type: " + toIndentedString(type) + "\n" +
                "    serverTime: " + toIndentedString(serverTime) + "\n" +
                "    serverVersion: " + toIndentedString(serverVersion) + "\n" +
                "    protocolVersion: " + toIndentedString(protocolVersion) + "\n" +
                "    motd: " + toIndentedString(motd) + "\n" +
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
