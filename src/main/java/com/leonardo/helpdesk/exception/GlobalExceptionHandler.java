package com.leonardo.helpdesk.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ErrorResponse buildError(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegisteredException(HttpServletRequest request, EmailAlreadyRegisteredException ex) {
        ErrorResponse error = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(InvalidTicketStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTicketStatusException(HttpServletRequest request, InvalidTicketStatusException ex) {
        ErrorResponse error = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(HttpServletRequest request, ResourceNotFoundException ex) {
        ErrorResponse error = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(RoleNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotAllowedException(HttpServletRequest request, RoleNotAllowedException ex) {
        ErrorResponse error = buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(TechnicianNotResponsibleException.class)
    public ResponseEntity<ErrorResponse> handleTechnicianNotResponsibleException(HttpServletRequest request, TechnicianNotResponsibleException ex) {
        ErrorResponse error = buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(TicketAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleTicketAlreadyAssignedException(HttpServletRequest request, TicketAlreadyAssignedException ex) {
        ErrorResponse error = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleUserNotActiveException(HttpServletRequest request, UserNotActiveException ex) {
        ErrorResponse error = buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ErrorResponse error = buildError(HttpStatus.BAD_REQUEST,  message, request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.BAD_REQUEST, "Malformed request body", request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.BAD_REQUEST, "Malformed parameter", request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.BAD_REQUEST, "Missing required parameter", request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.NOT_FOUND, "Route not found", request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed", request);
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(HttpServletRequest request, Exception ex) {
        ErrorResponse error = buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred", request);
        return ResponseEntity.status(error.status()).body(error);
    }
}
