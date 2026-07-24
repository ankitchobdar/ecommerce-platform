package org.project.orchestrator.exception;

import org.project.common.BaseMessage;
import org.project.common.ErrorResponse;
import org.project.common.Status;
import org.project.common.utility.MessageUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        BaseMessage baseMessage = MessageUtility.getBaseMessage(Status.FAILED, ex.getMessage());
        ErrorResponse error = new ErrorResponse(baseMessage);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = InventoryExhaustedException.class)
    public ResponseEntity<ErrorResponse> handleException(InventoryExhaustedException e) {
        BaseMessage baseMessage = MessageUtility.getBaseMessage(Status.FAILED, e.getMessage());
        ErrorResponse error = new ErrorResponse(baseMessage);
        return new ResponseEntity<>(error, HttpStatus.OK);
    }
}
