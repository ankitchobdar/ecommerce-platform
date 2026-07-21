package org.project.orchestrator.exception;

import org.springframework.http.HttpStatus;

public class InventoryExhaustedException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public InventoryExhaustedException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

}
