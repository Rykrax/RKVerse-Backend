package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.RedisService;
import dev.rykrax.rkverse.common.storage.IR2StorageService;
import dev.rykrax.rkverse.enums.ChapterStatus;
import dev.rykrax.rkverse.enums.ChapterUploadStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterAsyncService {

    private final ChapterRepository chapterRepository;
    private final IR2StorageService r2StorageService;
    private final RedisService redisService;
    private static final String CHAPTER_CACHE_PREFIX = "chapter:detail:";

    @Async("chapterUploadExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadChapterPagesAsync(
            Long chapterId,
            Long comicId,
            List<File> sortedFiles,
            File tempDir
    ) {
        log.info("Bắt đầu async upload {} trang cho Chapter ID: {}", sortedFiles.size(), chapterId);

        chapterRepository.findById(chapterId).ifPresent(chapter -> {
            chapter.setUploadStatus(ChapterUploadStatus.PROCESSING);
            chapterRepository.save(chapter);
        });

        String basePath = String.format("comics/%d/chapters/%d", comicId, chapterId);

        try {
            // upload tuần tự từng file tạm lên Cloudflare R2
            for (File file : sortedFiles) {
                String objectKey = String.format("%s/%s", basePath, file.getName());
                r2StorageService.uploadFileWithKey(file, objectKey, "image/webp");
            }

            chapterRepository.findById(chapterId).ifPresentOrElse(chapter -> {
                chapter.setUploadStatus(ChapterUploadStatus.SUCCESS);
                chapter.setStatus(ChapterStatus.PUBLISHED);
                chapter.setTotalPages(sortedFiles.size());
                chapter.setErrorMessage(null);
                chapterRepository.save(chapter);
                log.info("Upload hoàn tất thành công cho Chapter ID: {}", chapterId);

                String cacheKey = CHAPTER_CACHE_PREFIX + comicId + ":" + chapter.getChapterNumber();
                redisService.delete(cacheKey);
            }, () -> log.error("Không tìm thấy Chapter ID: {} để cập nhật SUCCESS", chapterId));

        } catch (Exception e) {
            log.error("Lỗi khi upload R2 cho Chapter ID {}: {}", chapterId, e.getMessage(), e);

            chapterRepository.findById(chapterId).ifPresent(chapter -> {
                chapter.setUploadStatus(ChapterUploadStatus.FAILED);
                chapter.setErrorMessage(e.getMessage());
                chapterRepository.save(chapter);
            });

        } finally {
            // dọn sạch thư mục tạm trên server
            FileSystemUtils.deleteRecursively(tempDir);
            log.info("Đã xóa thư mục tạm: {}", tempDir.getAbsolutePath());
        }
    }
}