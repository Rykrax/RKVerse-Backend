package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.common.storage.IR2StorageService;
import dev.rykrax.rkverse.enums.ComicUploadStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComicAsyncService {
    private final ComicRepository comicRepository;
    private final IR2StorageService r2StorageService;

    @Async // có thể chỉ định thread pool sẵn
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadCoverAsync(Long comicId, byte[] fileBytes, String contentType) {
        log.info("Bắt đầu xử lý upload ngầm cover cho Comic ID: {}", comicId);
        String objectKey = String.format("comics/%d/cover.webp", comicId);

        try {
            // upload byte[] lên Cloudflare R2
            String publicUrl = r2StorageService.uploadBytesWithKey(fileBytes, objectKey, contentType);

            // update comic trong db
            comicRepository.findById(comicId).ifPresentOrElse(comic -> {
                comic.setCoverPath(publicUrl);
                comic.setUploadStatus(ComicUploadStatus.SUCCESS);
                comicRepository.save(comic);
                log.info("Async upload thành công cho Comic ID: {}. URL: {}", comicId, publicUrl);
            }, () -> log.error("Không tìm thấy Comic ID: {} để cập nhật cover", comicId));

        } catch (Exception e) {
            log.error("Lỗi khi upload ngầm cover cho Comic ID {}: {}", comicId, e.getMessage(), e);
            comicRepository.findById(comicId).ifPresent(comic -> {
                comic.setUploadStatus(ComicUploadStatus.FAILED);
                comicRepository.save(comic);
            });
        }
    }
}
