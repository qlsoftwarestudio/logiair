package com.sportflow.gestor_reservas.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_shouldReturn404Response() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test resource not found");
        MockHttpServletRequest request = new MockHttpServletRequest();
        WebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleResourceNotFound(exception, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        GlobalExceptionHandler.ErrorResponse errorBody = response.getBody();
        assertNotNull(errorBody);
        assertEquals(404, errorBody.status());
        assertEquals("Test resource not found", errorBody.message());
        assertNull(errorBody.errors());
    }
}
