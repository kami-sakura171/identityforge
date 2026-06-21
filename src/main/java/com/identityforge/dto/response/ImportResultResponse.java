package com.identityforge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<ImportFailure> failures;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportFailure {
        private int row;
        private String reason;
    }
}
