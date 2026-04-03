package com.spotcamp.admin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogListResponse {
    private List<LogEntry> content;
    private long totalElements; // Estimated or bounded
    private int page;
    private int size;
}
