package org.example.rental.domain.exception;


public class RequestTimeoutException extends RuntimeException {
    public RequestTimeoutException(String msg) { super(msg); }
}
