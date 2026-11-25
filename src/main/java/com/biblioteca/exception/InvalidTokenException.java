package com.biblioteca.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String mensaje) {
        super(mensaje);
    }
}
