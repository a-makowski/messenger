package com.makowski.messenger.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("You have no acces to this data");
    }
}
