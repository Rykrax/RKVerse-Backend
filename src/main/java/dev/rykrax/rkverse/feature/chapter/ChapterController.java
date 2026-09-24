package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.ApiResponse;
import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.feature.chapter.dto.request.UploadChapterRequest;
import dev.rykrax.rkverse.feature.chapter.dto.request.ViewConfirmRequest;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterDetailResponse;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterResponse;
import dev.rykrax.rkverse.feature.chapter.dto.response.ViewStartResponse;
import dev.rykrax.rkverse.utils.ClientIdentifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChapterController {
    private final ChapterService chapterService;
    private final ViewService viewService;
    private final ClientIdentifier clientIdentifierUtil;

    @GetMapping("/comics/{comicId}/chapters")
    public ApiResponse<PageResponse<ChapterResponse>> getChapterByComic(
            @PathVariable Long comicId,
            @PageableDefault(page = 0, size = 10, sort = "chapterNumber", direction = Sort.Direction.DESC) Pageable pageable) {
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

    @PostMapping("/chapters/{chapterId}/views/start")
    public ApiResponse<ViewStartResponse> startReading(
            @PathVariable Long chapterId,
            HttpServletRequest req) {
        String clientKey = clientIdentifierUtil.resolveClientKey(req);
        ViewStartResponse response = viewService.startChapterReading(chapterId, clientKey);
        return new ApiResponse<>(200, "Đang xử lý", response);
    }

    @PostMapping("/chapters/{chapterId}/views/confirm")
    public ApiResponse<Void> confirmReading(
            @PathVariable Long chapterId,
            @RequestBody ViewConfirmRequest request,
            HttpServletRequest req) {
        String clientKey = clientIdentifierUtil.resolveClientKey(req);
        viewService.confirmChapterReading(chapterId, clientKey, request.readToken());
        return new ApiResponse<>(200, "Ghi nhận view thành công", null);
    }
}
