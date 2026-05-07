package de.emc.mitglieder.exception;

import de.emc.mitglieder.dto.error.ValidationErrorDto;

import java.util.List;

public class BusinessValidationException extends RuntimeException {

    private final List<ValidationErrorDto> validationErrors;

    public BusinessValidationException(List<ValidationErrorDto> validationErrors) {
        super("Validierungsfehler");
        this.validationErrors = validationErrors;
    }

    public List<ValidationErrorDto> getValidationErrors() {
        return validationErrors;
    }
}