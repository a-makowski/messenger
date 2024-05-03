package com.makowski.messenger.exception;

public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException() {
        super("There's no such user.");
    }
}
