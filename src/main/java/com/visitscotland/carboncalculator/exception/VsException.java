package com.visitscotland.carboncalculator.exception;

public class VsException extends RuntimeException {

    public VsException(String message) {
        super(message);
    }

    public VsException(String message, Throwable cause) {
        super(message, cause);
    }
}
