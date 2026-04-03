package com.spotcamp.module.visualmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBackgroundResponseDTO {

    private String url;
    private String message;

    public static UploadBackgroundResponseDTO of(String url) {
        return new UploadBackgroundResponseDTO(url, "Background image uploaded successfully");
    }
}
