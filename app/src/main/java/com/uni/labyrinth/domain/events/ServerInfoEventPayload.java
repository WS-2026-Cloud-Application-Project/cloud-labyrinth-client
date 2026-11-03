package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.model.Tile;

public class ServerInfoEventPayload {

    private String type; // CommandType als String (z.B. "CONNECT")
    private String serverTime;
    private String ServerVersion ;
    private String protocolVersion;
    private String motd;
    // Getter & Setter
    public String getType() {
        return type;
    }

    public String getServerTime() { return serverTime; }
    public String getServerVersion() { return ServerVersion;}
    public String getProtocolVersion() {return protocolVersion;    }
    public String getMotd() { return motd; }





}
