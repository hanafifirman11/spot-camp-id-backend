package com.spotcamp.campsite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteListResponse {

    private List<CampsiteResponse> content;
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

    public static CampsiteListResponse of(List<CampsiteResponse> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return CampsiteListResponse.builder()
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
