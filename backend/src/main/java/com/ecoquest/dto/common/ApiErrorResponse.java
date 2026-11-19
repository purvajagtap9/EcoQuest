package com.ecoquest.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Every non-2xx response from this API uses this shape:
 * { "success": false, "message": "...", "timestamp": "...", "errors": [...] }
 * `errors` is only populated for field-validation failures.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private Instant timestamp;
    private List<String> errors;

    public static ApiErrorResponse of(String message) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiErrorResponse of(String message, List<String> errors) {
        return ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .errors(errors)
                .build();
    }
}
