package com.identityforge.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_field_values",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "field_definition_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_definition_id", nullable = false)
    private CustomFieldDefinition fieldDefinition;

    @Column(length = 1000)
    private String value;
}
