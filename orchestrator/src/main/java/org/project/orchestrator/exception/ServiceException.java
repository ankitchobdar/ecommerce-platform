package org.project.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public ServiceException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
