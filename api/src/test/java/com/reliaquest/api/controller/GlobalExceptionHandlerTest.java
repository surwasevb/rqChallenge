package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.ServerErrorException;
import com.reliaquest.api.exception.TooManyRequestsException;
import com.reliaquest.api.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    public void handleNoRecordFoundExceptionReturnsNotFoundStatus() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Employee not found");
        ErrorResponse response = globalExceptionHandler.handleNoRecordFoundException(ex);

        assertEquals("Employee not found", response.getMessage());
    }

    @Test
    public void handleServerErrorExceptionReturnsInternalServerErrorStatus() {
        ServerErrorException ex = new ServerErrorException("Server error");
        ErrorResponse response = globalExceptionHandler.handleServerErrorException(ex);

        assertEquals("Server error", response.getMessage());
    }

    @Test
    public void handleHttpServerErrorExceptionReturnsInternalServerErrorStatus() {
        HttpServerErrorException ex = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse response = globalExceptionHandler.handleHttpServerErrorException(ex);

        assertEquals("Internal Server Error", response.getMessage());
    }

    @Test
    public void handleTooManyRequestExceptionReturnsTooManyRequestsStatus() {
        TooManyRequestsException ex = new TooManyRequestsException("Too many requests");
        ErrorResponse response = globalExceptionHandler.handleTooManyRequestException(ex);

        assertEquals("Too many requests to server", response.getMessage());
    }

    @Test
    public void handleConstraintViolationExceptionReturnsBadRequestStatus() {
        ConstraintViolationException ex = new ConstraintViolationException("Constraint violation", null);
        ResponseEntity<String> response = globalExceptionHandler.handleConstraintViolationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals("Constraint violation", response.getBody());
    }
}
