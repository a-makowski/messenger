package com.makowski.messenger.exception;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException() {
        super("Invalid Request");
    }
    public InvalidRequestException(String username) { super("Username " + username + " is already taken"); }
    public InvalidRequestException(int characters) { super("This message is " + characters + " charakters too long"); }
}
