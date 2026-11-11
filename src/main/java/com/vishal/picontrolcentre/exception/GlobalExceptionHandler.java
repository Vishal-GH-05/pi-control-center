package com.vishal.picontrolcentre.exception;

import com.vishal.picontrolcentre.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GlancesUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleGlancesUnavailable(
            GlancesUnavailableException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Glances is temporarily unavailable.",
                request
        );
    }

    @ExceptionHandler(RedisUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRedisUnavailable(
            RedisUnavailableException ex,
            HttpServletRequest request
    ) {
        log.error("Redis operation failed.", ex);

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Current metrics storage is temporarily unavailable.",
                request
        );
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseUnavailable(
            DataAccessResourceFailureException ex,
            HttpServletRequest request
    ) {
        log.error("Database resource failure.", ex);

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Historical metrics storage is temporarily unavailable.",
                request
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccessFailure(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        log.error("Database access operation failed.", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to complete the data operation.",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .findFirst()
                .orElse("Request validation failed.");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request body is missing or malformed.",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method is not supported for this endpoint.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported request content type.",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Requested resource was not found.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected application error on {}",
                request.getRequestURI(),
                ex
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = ApiErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
