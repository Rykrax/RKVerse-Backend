package dev.rykrax.rkverse.feature.chapter.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ChapterDetailResponse(
        Long id,
        Long comicId,
        BigDecimal chapterNumber,
        String title,
        int totalPages,
        List<String> pages,
        Long prevChapterId,
        Long nextChapterId
) {}