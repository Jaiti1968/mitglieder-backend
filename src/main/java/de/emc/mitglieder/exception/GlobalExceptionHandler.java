package de.emc.mitglieder.exception;

import de.emc.mitglieder.dto.error.ApiErrorResponse;
import de.emc.mitglieder.dto.error.ValidationErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Nicht gefunden: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        log.warn("Ungültige Anfrage: {} - {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ValidationErrorDto> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validierungsfehler",
                request.getRequestURI(),
                MDC.get("requestId"),
                validationErrors
        );

        log.warn("Validierungsfehler bei {}: {}", request.getRequestURI(), validationErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessValidationException(
            BusinessValidationException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validierungsfehler",
                request.getRequestURI(),
                MDC.get("requestId"),
                ex.getValidationErrors()
        );

        log.warn("Fachlicher Validierungsfehler bei {}: {}",
                request.getRequestURI(),
                ex.getValidationErrors());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateKey(
            DuplicateKeyException ex,
            HttpServletRequest request
    ) {
        log.warn("Duplicate Key bei {} - {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Datensatz existiert bereits oder verletzt eine Eindeutigkeitsregel",
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Datenbank-Regelverletzung bei Request {} - {}", request.getRequestURI(), ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Ungültige Daten oder Verstoß gegen Datenbankregeln",
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class,
            LockedException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationFailure(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("Fehlgeschlagener Login-Versuch bei {}", request.getRequestURI());

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Anmeldung nicht möglich.",
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        log.warn("Fachlicher HTTP-Fehler bei Request {} - {}",
                request.getRequestURI(),
                ex.getReason());

        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        ex.getReason(),
                        request.getRequestURI(),
                        MDC.get("requestId")
                ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unerwarteter Fehler bei Request {}", request.getRequestURI(), ex);

        return new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ein unerwarteter Fehler ist aufgetreten.",
                request.getRequestURI(),
                MDC.get("requestId")
        );
    }

    private ValidationErrorDto mapFieldError(FieldError fieldError) {
        return new ValidationErrorDto(
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );
    }
}