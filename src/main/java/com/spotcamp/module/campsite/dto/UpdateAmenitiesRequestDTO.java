package com.spotcamp.module.campsite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAmenitiesRequestDTO {

    @NotNull(message = "Amenity IDs are required")
    private List<Long> amenityIds;
}
