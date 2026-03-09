package com.todolist.api.exception;

import java.util.Map;

/**
 * Request is well-formed, but violates business rules for the current entity state.
 */
public class EntityValidationException extends RuntimeException {

    // field -> error message
    private final Map<String, String> errors;

    public EntityValidationException(Map<String, String> errors) {
        super("Entity validation failed");
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
