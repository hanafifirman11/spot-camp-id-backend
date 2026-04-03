package com.spotcamp.module.admin.theme.entity;

import com.spotcamp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "themes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;

    // JSON string containing CSS variables/tokens
    // e.g., { "--primary": "#FF5722", "--surface": "#FFFFFF" }
    @Column(name = "tokens_json", columnDefinition = "TEXT")
    private String tokensJson;
}
