package com.ablsoft.pima.advice;

import com.ablsoft.pima.exception.InvalidExcelException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> duplicate() {
        return ResponseEntity.badRequest()
                .body("Duplicate Product SKU + Purchase Date");
    }

    @ExceptionHandler(InvalidExcelException.class)
    public ResponseEntity<String> excel(InvalidExcelException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
