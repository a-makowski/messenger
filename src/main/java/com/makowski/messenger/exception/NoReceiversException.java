package com.makowski.messenger.exception;

public class NoReceiversException extends RuntimeException {
    public NoReceiversException() {
        super("Invalid Receivers ID's");
    }
}
