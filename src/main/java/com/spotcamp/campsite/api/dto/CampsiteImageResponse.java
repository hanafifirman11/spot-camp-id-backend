package com.spotcamp.campsite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteImageResponse {

    private Long id;
    private String url;
    private String caption;
    private Boolean isPrimary;
    private Integer displayOrder;
}
