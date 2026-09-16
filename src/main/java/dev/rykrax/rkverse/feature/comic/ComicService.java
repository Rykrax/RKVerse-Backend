package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.enums.ComicUploadStatus;
import dev.rykrax.rkverse.enums.ErrorCode;
import dev.rykrax.rkverse.exception.AppException;
import dev.rykrax.rkverse.feature.comic.dto.request.CreateComicRequest;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicDetailResponse;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicResponse;
import dev.rykrax.rkverse.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComicService implements IComicService {
    private final ComicRepository comicRepository;
    private final ComicMapper comicMapper;
    private final ComicAsyncService comicAsyncService;

    @Override
    public PageResponse<ComicResponse> getComics(Pageable pageable) {
        Page<Comic> comicPage = comicRepository.findAll(pageable);

        Page<ComicResponse> responsePage = comicPage.map(comicMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    public ComicDetailResponse getComic(Long id) {
        Comic comic = comicRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.COMIC_NOT_FOUND));
        return comicMapper.toDetailResponse(comic);
    }

    @Override
    @Transactional
    public ComicResponse createComic(CreateComicRequest request) {
        // xử lý lỗi IO trước
        byte[] fileBytes = null;
        String contentType = "image/webp";
        boolean hasCoverImage = request.coverImage() != null && !request.coverImage().isEmpty();

        if (hasCoverImage) {
            try {
                fileBytes = request.coverImage().getBytes();
                if (request.coverImage().getContentType() != null) {
                    contentType = request.coverImage().getContentType();
                }
            } catch (IOException e) {
                log.error("Lỗi đọc file upload", e);
                throw new RuntimeException("Lỗi đọc file upload", e);
            }
        }

        // lưu db
        Comic comic = comicMapper.toEntity(request);
        if (request.slug() == null || request.slug().isBlank()) {
            String slug = SlugUtils.toSlug(request.title());
            comic.setSlug(slug);
        } else {
            comic.setSlug(request.slug().trim());
        }

        if (hasCoverImage) {
            comic.setUploadStatus(ComicUploadStatus.PENDING);
        }
        Comic savedComic = comicRepository.save(comic);

        // xử lý async sau khi commit vào db thành công
        if (hasCoverImage) {
            Long comicId = savedComic.getId();
            byte[] finalBytes = fileBytes;
            String finalContentType = contentType;

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    comicAsyncService.uploadCoverAsync(comicId, finalBytes, finalContentType);
                }
            });
        }

        log.info("Đã tạo mới Comic thành công với ID: {}, slug: {}", comic.getId(), comic.getSlug());
        return comicMapper.toResponse(savedComic);
    }

    @Override
    public void delete(Long id) {
        Comic comic = comicRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.COMIC_NOT_FOUND));
        comic.setDeletedAt(Instant.now());
        comicRepository.save(comic);
    }
}
