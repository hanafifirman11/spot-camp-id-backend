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
public class AdminBusinessCampsiteListResponse {
    private List<AdminBusinessCampsiteResponse> content;
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

    public static AdminBusinessCampsiteListResponse of(List<AdminBusinessCampsiteResponse> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return AdminBusinessCampsiteListResponse.builder()
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
