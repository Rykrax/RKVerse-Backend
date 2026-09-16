package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.common.ApiResponse;
import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.feature.comic.dto.request.CreateComicRequest;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicDetailResponse;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicResponse;
import dev.rykrax.rkverse.feature.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comics")
@RequiredArgsConstructor
public class ComicController {
    private final ComicService comicService;

    @GetMapping("")
    public ApiResponse<PageResponse<ComicResponse>> getComics(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ComicResponse> response = comicService.getComics(pageable);
        return new ApiResponse<>(200, "Danh sách truyện", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ComicDetailResponse> getComic(@PathVariable Long id) {
        ComicDetailResponse response = comicService.getComic(id);
        return new ApiResponse<>(200, "Thông tin truyện", response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ComicResponse> create(@Valid @ModelAttribute CreateComicRequest request) {
        ComicResponse response = comicService.createComic(request);
        return new ApiResponse<>(200, "Thêm truyện thành công", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        comicService.delete(id);
        return new ApiResponse<>(200, "Xóa thành công", null);
    }
}
