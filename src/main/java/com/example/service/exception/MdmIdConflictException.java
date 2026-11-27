package com.example.service.exception;

public class MdmIdConflictException extends RuntimeException {
    public MdmIdConflictException(String message) {
        super(message);
    }
}
