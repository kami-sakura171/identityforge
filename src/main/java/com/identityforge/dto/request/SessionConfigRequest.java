package com.identityforge.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionConfigRequest {

    @NotNull(message = "Timeout minutes is required")
    @Min(value = 5, message = "Timeout must be at least 5 minutes")
    @Max(value = 480, message = "Timeout must not exceed 480 minutes")
    private Integer timeoutMinutes;
}
