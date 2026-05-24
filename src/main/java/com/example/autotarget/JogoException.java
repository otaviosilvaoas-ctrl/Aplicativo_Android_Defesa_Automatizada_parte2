package com.example.autotarget;

public class JogoException extends Exception {
    public JogoException(String message) {
        super(message);
    }

    public JogoException(String message, Throwable cause) {
        super(message, cause);
    }
}