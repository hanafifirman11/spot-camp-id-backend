package com.spotcamp.module.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCampsiteListResponseDTO {
    private List<AdminCampsiteSummaryResponseDTO> content;
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

    public static AdminCampsiteListResponseDTO of(List<AdminCampsiteSummaryResponseDTO> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return AdminCampsiteListResponseDTO.builder()
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
