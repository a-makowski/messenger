package com.makowski.messenger.exception;

public class EmptyListException extends RuntimeException {
    public EmptyListException(String list) {
        super("Your " + list +" List is empty.");
    }
}
