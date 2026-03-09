package com.todolist.api.exception;

public class InvalidSortPropertyException extends RuntimeException {

    public InvalidSortPropertyException(String property) {
        super("Unsupported sort property: " + property);
    }
}
