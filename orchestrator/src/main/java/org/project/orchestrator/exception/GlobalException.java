package org.project.orchestrator.exception;

import org.project.common.inventory.InventoryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }

    @ExceptionHandler(value = InventoryExhaustedException.class)
    public ResponseEntity<String> handleException(InventoryExhaustedException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
