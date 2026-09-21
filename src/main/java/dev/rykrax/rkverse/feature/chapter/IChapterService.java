package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.feature.chapter.dto.request.UploadChapterRequest;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterDetailResponse;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IChapterService {
    PageResponse<ChapterResponse> getChapters(Long comicId, Pageable pageable);
    ChapterDetailResponse getChapterDetail(Long comicId, BigDecimal chapterNumber);
    void create(Long comicId, UploadChapterRequest request);
}
