package com.spotcamp.campsite.api.dto;

import com.spotcamp.campsite.domain.CampsiteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampsiteStatusRequest {

    @NotNull(message = "Status is required")
    private CampsiteStatus status;
}
