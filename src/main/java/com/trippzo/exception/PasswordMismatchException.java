package com.trippzo.exception;

public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException() {
        super("Паролите не съвпадат!");
    }
}