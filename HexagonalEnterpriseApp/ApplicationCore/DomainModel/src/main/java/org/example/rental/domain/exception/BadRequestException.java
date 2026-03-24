package org.example.rental.domain.exception;


public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) { super(msg); }
}
