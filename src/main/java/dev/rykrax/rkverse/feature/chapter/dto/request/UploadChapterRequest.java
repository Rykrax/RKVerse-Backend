package dev.rykrax.rkverse.feature.chapter.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record UploadChapterRequest(
        @Size(max = 255, message = "Tiêu đề chương không được vượt quá 255 ký tự")
        String title,

        @NotNull(message = "Số chương không được để trống")
        @PositiveOrZero(message = "Số chương phải lớn hơn hoặc bằng 0")
        @Digits(integer = 5, fraction = 1, message = "Số chương chỉ tối đa 6 chữ số phần nguyên và 1 chữ số thập phân")
        BigDecimal chapterNumber,

        @NotEmpty(message = "Danh sách ảnh của chương không được để trống")
        List<MultipartFile> files
) {
        public UploadChapterRequest {
                title = (title == null || title.isBlank()) ? null : title.trim();
        }
}
