package dev.rykrax.rkverse.feature.chapter.dto.response;

import java.math.BigDecimal;

public record ChapterResponse(
        Long id,
        Long comicId,
        BigDecimal chapterNumber,
        Integer totalPages,
        Long views
) {
}
