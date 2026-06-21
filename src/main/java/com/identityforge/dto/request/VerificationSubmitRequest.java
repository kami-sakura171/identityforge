package com.identityforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerificationSubmitRequest {

    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "Document number is required")
    @Size(max = 100)
    private String documentNumber;

    @Size(max = 100)
    private String realName;
}
