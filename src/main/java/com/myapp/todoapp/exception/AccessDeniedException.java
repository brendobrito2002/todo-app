package com.myapp.todoapp.exception;

@SuppressWarnings("serial")
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
