package com.spotcamp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogListResponseDTO {
    private List<LogEntryDTO> content;
    private long totalElements; // Estimated or bounded
    private int page;
    private int size;
}
