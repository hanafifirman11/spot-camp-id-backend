package com.spotcamp.module.campsite.dto;

import com.spotcamp.module.campsite.entity.CampsiteStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampsiteStatusRequestDTO {

    @NotNull(message = "Status is required")
    private CampsiteStatus status;
}
