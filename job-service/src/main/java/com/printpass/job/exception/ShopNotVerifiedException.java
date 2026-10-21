// exception/ShopNotVerifiedException.java
package com.printpass.job.exception;

public class ShopNotVerifiedException extends RuntimeException {
    public ShopNotVerifiedException(String message) {
        super(message);
    }
}