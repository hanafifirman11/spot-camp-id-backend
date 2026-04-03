package com.spotcamp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDTO {
    private String timestamp;
    private String level;
    private String thread;
    private String logger;
    private String message;
    private String raw; // Fallback if parsing fails
}
