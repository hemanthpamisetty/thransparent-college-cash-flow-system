package com.college.cashflow.exception;

import com.college.cashflow.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testBadCredentialsExceptionReturns401() {
        BadCredentialsException ex = new BadCredentialsException("Invalid username or password");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    void testAccessDeniedExceptionReturns403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Access denied"));
    }

    @Test
    void testResourceNotFoundExceptionReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Department not found with id: 99");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Department not found with id: 99", response.getBody().getMessage());
    }

    @Test
    void testResourceConflictExceptionReturns409() {
        ResourceConflictException ex = new ResourceConflictException("Department name already exists");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Department name already exists", response.getBody().getMessage());
    }

    @Test
    void testIllegalArgumentExceptionReturns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Start date cannot be after end date");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Start date cannot be after end date", response.getBody().getMessage());
    }

    @Test
    void testIllegalStateExceptionReturns400() {
        IllegalStateException ex = new IllegalStateException("Department Admin is not assigned to an active department");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Department Admin is not assigned to an active department", response.getBody().getMessage());
    }

    @Test
    void testDataIntegrityViolationExceptionSanitizesMessageAndReturns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Cannot delete or update a parent row: foreign key constraint fails (`college_cashflow`.`transactions`, CONSTRAINT `fk_transactions_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`))"
        );
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        // Verify no raw SQL or table names are leaked to the client
        assertFalse(response.getBody().getMessage().contains("college_cashflow"));
        assertFalse(response.getBody().getMessage().contains("fk_transactions_category"));
        assertTrue(response.getBody().getMessage().contains("Database constraint violation"));
    }

    @Test
    void testGeneralExceptionSanitizesMessageAndReturns500() {
        NullPointerException ex = new NullPointerException("Null reference at Line 42 in InternalClass");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        // Verify internal stack/class details are not leaked
        assertFalse(response.getBody().getMessage().contains("InternalClass"));
        assertTrue(response.getBody().getMessage().contains("unexpected internal server error"));
    }
}
