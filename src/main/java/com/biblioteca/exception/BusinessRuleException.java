package com.biblioteca.exception;

/** Se lanza cuando una operación viola una regla de negocio (sin stock, límite de préstamos, etc.). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String mensaje) {
        super(mensaje);
    }
}
