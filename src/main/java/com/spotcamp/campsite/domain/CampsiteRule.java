package com.spotcamp.campsite.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing rules/policies for a campsite
 */
@Entity
@Table(name = "campsite_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampsiteRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", insertable = false, updatable = false)
    private Long campsiteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campsite_id", nullable = false)
    private Campsite campsite;

    @Column(name = "rule_text", nullable = false, length = 500)
    private String ruleText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}
