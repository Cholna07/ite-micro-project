package kh.edu.istad.platform.exception;


import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DownStreamServiceException.class)
    public ResponseEntity<String> handleDownStreamServiceException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "Timestamp:", LocalDateTime.now(),
                        "status: ", HttpStatus.INTERNAL_SERVER_ERROR,
                        "Message: ", ex.getMessage()

                ).toString());}
}
