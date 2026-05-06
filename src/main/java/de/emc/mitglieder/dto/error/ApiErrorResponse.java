package de.emc.mitglieder.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String requestId;
    private List<ValidationErrorDto> validationErrors;

    public ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            String requestId
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
        this.validationErrors = null;
    }
}