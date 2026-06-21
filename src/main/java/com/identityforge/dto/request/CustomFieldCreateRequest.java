package com.identityforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CustomFieldCreateRequest {

    @NotBlank(message = "Field name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Field label is required")
    @Size(max = 200)
    private String label;

    @NotBlank(message = "Field type is required")
    private String fieldType; // TEXT, DROPDOWN, BOOLEAN

    private List<String> options; // For DROPDOWN

    private Boolean isRequired = false;

    private Integer displayOrder = 0;
}
