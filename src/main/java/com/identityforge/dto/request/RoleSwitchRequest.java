package com.identityforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleSwitchRequest {

    @NotBlank(message = "Role is required")
    private String contextualRole;
}
