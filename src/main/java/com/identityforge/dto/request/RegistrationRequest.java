package com.identityforge.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String displayName;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$",
             message = "Date of birth must be in MM/DD/YYYY format")
    private String dateOfBirth;

    @NotNull(message = "Security question selection is required")
    @Min(value = 1)
    @Max(value = 8)
    private Integer securityQuestionId;

    @NotBlank(message = "Security answer is required")
    @Size(max = 255)
    private String securityAnswer;

    private Boolean acceptTos = false;
}
