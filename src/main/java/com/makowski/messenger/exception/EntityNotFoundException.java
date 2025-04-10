package com.makowski.messenger.exception;

public class EntityNotFoundException extends RuntimeException { 

    public EntityNotFoundException(Long id, Class<?> entity) { 
        super("The " + entity.getSimpleName().toLowerCase() + " with id '" + id + "' does not exist.");
    }

    public EntityNotFoundException(String username, Class<?> entity) {
        super("The " + entity.getSimpleName().toLowerCase() + " with name '" + username + "' does not exist.");
    }
}