package com.spotcamp.visualmap.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBackgroundResponse {

    private String url;
    private String message;

    public static UploadBackgroundResponse of(String url) {
        return new UploadBackgroundResponse(url, "Background image uploaded successfully");
    }
}
