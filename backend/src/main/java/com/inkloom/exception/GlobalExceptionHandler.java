package com.inkloom.exception;

import com.inkloom.dto.response.ApiResponse;
import com.inkloom.dto.response.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorDetails errorDetails = new ErrorDetails(
                "VALIDATION_ERROR",
                "Input validation failed for one or more fields",
                request.getRequestURI(),
                errors
        );

        log.warn("Validation error on path {}: {}", request.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Validation error", errorDetails));
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleStateTransition(
            InvalidStateTransitionException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "INVALID_STATE_TRANSITION",
                ex.getMessage(),
                request.getRequestURI()
        );
        log.warn("Invalid state transition on path {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage(), errorDetails));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "DUPLICATE_RESOURCE",
                ex.getMessage(),
                request.getRequestURI()
        );
        log.warn("Duplicate resource conflict on path {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(ex.getMessage(), errorDetails));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        log.info("Resource not found on path {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage(), errorDetails));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "UNAUTHORIZED",
                ex.getMessage() != null ? ex.getMessage() : "Authentication is required to access this resource",
                request.getRequestURI()
        );

        log.warn("Unauthorized access on path {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ex.getMessage(), errorDetails));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "FORBIDDEN",
                ex.getMessage() != null ? ex.getMessage() : "Access denied to requested resource",
                request.getRequestURI()
        );

        log.warn("Access denied on path {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Access denied", errorDetails));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                "METHOD_NOT_ALLOWED",
                "HTTP method " + ex.getMethod() + " is not supported for this endpoint",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.fail("Method not allowed", errorDetails));
    }

    // ===================================================================
    // Google Gemini AI Integration Exception Handlers
    // ===================================================================

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiConfigurationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiConfiguration(
            com.inkloom.integration.ai.exception.AiConfigurationException ex, HttpServletRequest request) {
        log.error("AI Configuration Error on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_CONFIGURATION_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail(ex.getMessage(), errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiAuth(
            com.inkloom.integration.ai.exception.AiAuthenticationException ex, HttpServletRequest request) {
        log.error("AI Provider Authentication Failed on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_AUTHENTICATION_ERROR",
                "Downstream AI service credentials failed authentication. Verify server GOOGLE_API_KEY.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.fail("AI authentication failure", errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiRateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiRateLimit(
            com.inkloom.integration.ai.exception.AiRateLimitException ex, HttpServletRequest request) {
        log.warn("AI Rate Limit Exceeded on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_RATE_LIMIT_EXCEEDED",
                "AI service rate limit exceeded. Please retry after a brief pause.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.fail("Rate limit exceeded", errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiTimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiTimeout(
            com.inkloom.integration.ai.exception.AiTimeoutException ex, HttpServletRequest request) {
        log.error("AI Timeout on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_TIMEOUT",
                "The AI review request exceeded the server response timeout threshold.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.fail("AI request timeout", errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiModelNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiModelNotFound(
            com.inkloom.integration.ai.exception.AiModelNotFoundException ex, HttpServletRequest request) {
        log.error("AI Model Not Found on path {}: model={}", request.getRequestURI(), ex.getModel());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_MODEL_NOT_FOUND",
                "The configured Gemini model is currently unavailable or unsupported.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("AI model unavailable", errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiDisabled(
            com.inkloom.integration.ai.exception.AiDisabledException ex, HttpServletRequest request) {
        log.info("AI invocation on path {} rejected: AI features disabled", request.getRequestURI());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_FEATURE_DISABLED",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage(), errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiProviderException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiProvider(
            com.inkloom.integration.ai.exception.AiProviderException ex, HttpServletRequest request) {
        log.error("AI Provider downstream failure on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_PROVIDER_ERROR",
                "Downstream AI service failed to complete evaluation. Technical details logged.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.fail("AI provider error", errorDetails));
    }

    @ExceptionHandler(com.inkloom.integration.ai.exception.AiException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralAiException(
            com.inkloom.integration.ai.exception.AiException ex, HttpServletRequest request) {
        log.error("Generic AI Engine exception on path {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                "AI_ENGINE_ERROR",
                "AI governance engine encountered an error.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("AI engine error", errorDetails));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled server exception on path {}", request.getRequestURI(), ex);

        ErrorDetails errorDetails = new ErrorDetails(
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please contact the administrator.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Internal server error", errorDetails));
    }
}
