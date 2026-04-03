package com.spotcamp.campsite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteSummaryResponse {

    private long total;
    private long active;
    private long inactive;
    private long suspended;
}
