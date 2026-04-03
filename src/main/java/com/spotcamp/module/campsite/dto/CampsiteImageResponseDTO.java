package com.spotcamp.module.campsite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteImageResponseDTO {

    private Long id;
    private String url;
    private String caption;
    private Boolean isPrimary;
    private Integer displayOrder;
}
