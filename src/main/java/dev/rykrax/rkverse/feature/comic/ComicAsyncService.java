package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.common.storage.IR2StorageService;
import dev.rykrax.rkverse.enums.ComicUploadStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComicAsyncService {
    private final ComicRepository comicRepository;
    private final IR2StorageService r2StorageService;
    private final TransactionTemplate transactionTemplate;

    @Async
    public void uploadCoverAsync(Long comicId, byte[] fileBytes, String contentType) {
        String objectKey = String.format("comics/%d/cover.webp", comicId);

        try {
            // upload byte[] lên Cloudflare R2
            String publicUrl = r2StorageService.uploadBytesWithKey(fileBytes, objectKey, contentType);

            transactionTemplate.executeWithoutResult(status -> {
                comicRepository.findById(comicId).ifPresent(comic -> {
                    comic.setCoverPath(publicUrl);
                    comic.setUploadStatus(ComicUploadStatus.SUCCESS);
                });
            });
            log.info("Upload thành công cho Comic ID: {}", comicId);
        } catch (Exception e) {
            log.error("Lỗi khi upload ngầm cover cho Comic ID {}: {}", comicId, e.getMessage(), e);
            transactionTemplate.executeWithoutResult(status -> {
                comicRepository.findById(comicId).ifPresent(comic -> {
                    comic.setUploadStatus(ComicUploadStatus.FAILED);
                });
            });
        }
    }
}
