package dev.rykrax.rkverse.feature.comic.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record CreateComicRequest(
        @NotBlank(message = "Tên truyện không được để trống")
        String title,

        String slug,
        String author,
        String description,
        MultipartFile coverImage
) {
}
