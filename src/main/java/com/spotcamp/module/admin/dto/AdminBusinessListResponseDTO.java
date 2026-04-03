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
public class AdminBusinessListResponseDTO {
    private List<AdminBusinessSummaryResponseDTO> content;
    private PageMetadata page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    public static AdminBusinessListResponseDTO of(List<AdminBusinessSummaryResponseDTO> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return AdminBusinessListResponseDTO.builder()
            .content(content)
            .page(PageMetadata.builder()
                .number(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build())
            .build();
    }
}
