package com.printpass.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PrintPassException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public PrintPassException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }
}