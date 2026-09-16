package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.ApiResponse;
import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.feature.chapter.dto.request.UploadChapterRequest;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterDetailResponse;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;

    @GetMapping("/comics/{comicId}/chapters")
    public ApiResponse<PageResponse<ChapterResponse>> getChapterByComic(
            @PathVariable Long comicId,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ChapterResponse> response = chapterService.getChapters(comicId, pageable);
        return new ApiResponse<>(200, "Danh sách chapter", response);
    }

    @GetMapping("/comics/{comicId}/chapters/{chapterNumber}")
    public ApiResponse<ChapterDetailResponse> getChapterDetail(
            @PathVariable Long comicId,
            @PathVariable BigDecimal chapterNumber
    ) {
        ChapterDetailResponse response = chapterService.getChapterDetail(comicId, chapterNumber);
        return new ApiResponse<>(200,"Thông tin chapter", response);
    }

    @PostMapping("/comics/{comicId}/chapters")
    public ApiResponse<Void> create(
            @PathVariable Long comicId,
            @ModelAttribute UploadChapterRequest request) {
        chapterService.create(comicId, request);
        return new ApiResponse<>(200, "Thêm chương truyện thành công", null);
    }
}
