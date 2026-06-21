package com.identityforge.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.identityforge.dto.request.CustomFieldCreateRequest;
import com.identityforge.exception.BadRequestException;
import com.identityforge.exception.ResourceNotFoundException;
import com.identityforge.model.CustomFieldDefinition;
import com.identityforge.model.User;
import com.identityforge.model.enums.CustomFieldType;
import com.identityforge.repository.CustomFieldDefinitionRepository;
import com.identityforge.repository.UserRepository;
import com.identityforge.service.common.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomFieldService {

    private static final int MAX_FIELDS = 20;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CustomFieldDefinitionRepository definitionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<CustomFieldDefinition> getAllFields() {
        return definitionRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    @Transactional
    public CustomFieldDefinition createField(CustomFieldCreateRequest request, Long adminId) {
        // Enforce max 20
        long count = definitionRepository.countByIsActiveTrue();
        if (count >= MAX_FIELDS) {
            throw new BadRequestException("Maximum of " + MAX_FIELDS + " custom fields allowed");
        }

        // Check name uniqueness
        if (definitionRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new BadRequestException("Custom field name '" + request.getName() + "' already exists");
        }

        // Validate type
        CustomFieldType fieldType;
        try {
            fieldType = CustomFieldType.valueOf(request.getFieldType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid field type: " + request.getFieldType());
        }

        // Serialize options for dropdown
        String optionsJson = null;
        if (fieldType == CustomFieldType.DROPDOWN && request.getOptions() != null) {
            try {
                optionsJson = objectMapper.writeValueAsString(request.getOptions());
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid options format");
            }
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        CustomFieldDefinition field = CustomFieldDefinition.builder()
                .name(request.getName())
                .label(request.getLabel())
                .fieldType(fieldType)
                .optionsJson(optionsJson)
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(true)
                .createdBy(admin)
                .build();
        field = definitionRepository.save(field);

        auditLogService.log(adminId, "CUSTOM_FIELD_CREATED", "CustomFieldDefinition",
                field.getId(), "Created field: " + field.getName());
        log.info("Custom field created: {} by admin {}", field.getName(), adminId);

        return field;
    }

    @Transactional
    public CustomFieldDefinition updateField(Long fieldId, CustomFieldCreateRequest request, Long adminId) {
        CustomFieldDefinition field = definitionRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found"));

        if (request.getLabel() != null) field.setLabel(request.getLabel());
        if (request.getIsRequired() != null) field.setIsRequired(request.getIsRequired());
        if (request.getDisplayOrder() != null) field.setDisplayOrder(request.getDisplayOrder());
        if (request.getOptions() != null && field.getFieldType() == CustomFieldType.DROPDOWN) {
            try {
                field.setOptionsJson(objectMapper.writeValueAsString(request.getOptions()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Invalid options format");
            }
        }

        field = definitionRepository.save(field);
        auditLogService.log(adminId, "CUSTOM_FIELD_UPDATED", "CustomFieldDefinition",
                field.getId(), "Updated field: " + field.getName());
        return field;
    }

    @Transactional
    public void deleteField(Long fieldId, Long adminId) {
        CustomFieldDefinition field = definitionRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found"));
        field.setIsActive(false);
        definitionRepository.save(field);

        auditLogService.log(adminId, "CUSTOM_FIELD_DELETED", "CustomFieldDefinition",
                field.getId(), "Soft-deleted field: " + field.getName());
        log.info("Custom field deleted: {} by admin {}", field.getName(), adminId);
    }
}
