package com.uni.labyrinth.domain.events;

public class ConnectAckPayload {

    private String type; // CommandType als String (z.B. "CONNECT")
    private String playerId;
    private String identifierToken;
    // Getter & Setter
    public String getType() {
        return type;
    }

    public String getPlayerId() { return playerId;}
    public String getIdentifierToken() {return identifierToken;}

}
