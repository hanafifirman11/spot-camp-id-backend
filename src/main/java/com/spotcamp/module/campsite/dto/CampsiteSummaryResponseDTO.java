package com.spotcamp.module.campsite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteSummaryResponseDTO {

    private long total;
    private long active;
    private long inactive;
    private long suspended;
}
