package com.uni.labyrinth.domain.events;

import com.uni.labyrinth.domain.enums.ErrorCode;


public class ActionErrorEventPayload {

    private String type;

    private ErrorCode errorCode;
    private String message;
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
