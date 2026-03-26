package com.printpass.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Yeh annotation isey poore system ka Error Catcher bana deti hai!
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PrintPassException.class)
    public ResponseEntity<Map<String, Object>> handlePrintPassException(PrintPassException ex) {

        // Frontend ke liye ek neat and clean JSON object ban banana
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", ex.getStatus().value());
        errorResponse.put("error", ex.getStatus().getReasonPhrase());
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // Agar koi aesa error aagaya jo humne catch nahi kiya (jaise NullPointer)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", 500);
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "System mein koi masla hai, hamari team isay dekh rahi hai!");

        return new ResponseEntity<>(errorResponse, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}