package com.sumukh.aaa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
  public ResponseEntity<?> notFound(jakarta.persistence.EntityNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
      "error","Not Found",
      "message", ex.getMessage()
    ));
  }
}
