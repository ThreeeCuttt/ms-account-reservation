package com.example.controller;

import com.example.service.exception.ClientNotFoundException;
import com.example.service.exception.MdmIdConflictException;
import com.example.upg4.api.model.ErrorCode;
import com.example.upg4.api.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ClientNotFoundException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(ErrorCode.CLIENT_NOT_FOUND);
        error.setErrorDescription(ex.getMessage());
        error.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409
    @ExceptionHandler(MdmIdConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(MdmIdConflictException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(ErrorCode.MDM_ID_CONFLICT);
        error.setErrorDescription(ex.getMessage());
        error.setStatusCode(HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Обр400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации данных");

        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(ErrorCode.VALIDATION_ERROR);
        error.setErrorDescription(errorMsg);
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //  500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(ErrorCode.INTERNAL_ERROR);
        error.setErrorDescription("Внутренняя ошибка сервера: " + ex.getMessage());
        error.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
