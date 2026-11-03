package com.uni.labyrinth.domain.commands;

public class ConnectCommandPayload {


    private String type; // CommandType als String (z.B. "CONNECT")
    private String username;
    private String identifierToken; // optional

    // Konstruktor ohne identifierToken
    public ConnectCommandPayload(String type, String username) {
        this.type = type;
        this.username = username;
    }

    // Konstruktor mit identifierToken
    public ConnectCommandPayload(String type, String username, String identifierToken) {
        this.type = type;
        this.username = username;
        this.identifierToken = identifierToken;
    }

    // Getter & Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdentifierToken() {
        return identifierToken;
    }

    public void setIdentifierToken(String identifierToken) {
        this.identifierToken = identifierToken;
    }
}
