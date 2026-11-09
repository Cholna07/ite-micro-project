package kh.edu.istad.platform.restapi.exception;

import org.springframework.http.HttpStatus;

public class RestApiException extends RuntimeException {

    private final HttpStatus status;

    public RestApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
