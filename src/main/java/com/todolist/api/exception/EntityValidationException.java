package com.todolist.api.exception;

import java.util.Map;

public class EntityValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public EntityValidationException(Map<String, String> errors) {
        super("Entity validation failed");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
