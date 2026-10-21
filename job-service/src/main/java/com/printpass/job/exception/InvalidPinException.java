// exception/InvalidPinException.java
package com.printpass.job.exception;

public class InvalidPinException extends RuntimeException {
    public InvalidPinException(String message) {
        super(message);
    }
}