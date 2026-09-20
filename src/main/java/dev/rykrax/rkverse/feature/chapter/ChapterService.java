package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.enums.ChapterStatus;
import dev.rykrax.rkverse.enums.ChapterUploadStatus;
import dev.rykrax.rkverse.enums.ErrorCode;
import dev.rykrax.rkverse.exception.AppException;
import dev.rykrax.rkverse.feature.chapter.dto.request.UploadChapterRequest;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterDetailResponse;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterResponse;
import dev.rykrax.rkverse.feature.comic.Comic;
import dev.rykrax.rkverse.feature.comic.ComicRepository;
import dev.rykrax.rkverse.utils.ImageValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterService implements IChapterService {
    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final ChapterMapper chapterMapper;
    private final ChapterAsyncService chapterAsyncService;
    @Value("${cloudflare.r2.public-domain}")
    private String publicDomain;

    @Override
    public PageResponse<ChapterResponse> getChapters(Long comicId, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findByComicIdAndStatusAndDeletedAtIsNull(
                comicId,
                ChapterStatus.PUBLISHED,
                pageable
        );
        Page<ChapterResponse> responseChapters = chapters.map(chapterMapper::toResponse);
        return PageResponse.from(responseChapters);
    }

    @Override
    public ChapterDetailResponse getDetailChapter(Long comicId, Long chapterId) {
        Chapter chapter = chapterRepository.findByIdAndStatusAndDeletedAtIsNull(chapterId, ChapterStatus.PUBLISHED)
                .filter(c -> c.getComic().getId().equals(comicId))
                .orElseThrow(() -> new EntityNotFoundException("Chương không tồn tại hoặc chưa được xuất bản"));
        String cleanDomain = publicDomain.endsWith("/")
                ? publicDomain.substring(0, publicDomain.length() - 1)
                : publicDomain;
        String basePath = chapter.getStoragePath() != null
                ? chapter.getStoragePath()
                : String.format("comics/%d/chapters/%d", comicId, chapter.getId());

        List<String> pages = IntStream.rangeClosed(1, chapter.getTotalPages())
                .mapToObj(i -> String.format("%s/%s/%03d.webp", cleanDomain, basePath, i))
                .toList();
        Long prevChapterId = chapterRepository.findPrevChapterId(comicId, chapter.getChapterNumber()).orElse(null);
        Long nextChapterId = chapterRepository.findNextChapterId(comicId, chapter.getChapterNumber()).orElse(null);
        chapterRepository.save(chapter);
        return chapterMapper.toDetailResponse(chapter, pages, prevChapterId, nextChapterId);
    }

    @Override
    public ChapterDetailResponse getChapterDetail(Long comicId, BigDecimal chapterNumber) {
        // tìm chapter theo comicId + chapterNumber với status PUBLISHED
        Chapter chapter = chapterRepository
                .findActivityChapter(comicId, chapterNumber, ChapterStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Chương không tồn tại hoặc đã bị xóa"));

        // chuẩn hóa domain
        String cleanDomain = publicDomain.endsWith("/")
                ? publicDomain.substring(0, publicDomain.length() - 1)
                : publicDomain;

        // đường dẫn storage
        String basePath = (chapter.getStoragePath() != null && !chapter.getStoragePath().isBlank())
                ? chapter.getStoragePath()
                : String.format("comics/%d/chapters/%d", comicId, chapter.getId());

        // sinh danh sách link ảnh
        int totalPages = chapter.getTotalPages() != null ? chapter.getTotalPages() : 0;
        List<String> pages = IntStream.rangeClosed(1, totalPages)
                .mapToObj(i -> String.format("%s/%s/%03d.webp", cleanDomain, basePath, i))
                .toList();

        // điều hướng chương trước / sau
        Long prevChapterId = chapterRepository.findPrevChapterId(comicId, chapter.getChapterNumber()).orElse(null);
        Long nextChapterId = chapterRepository.findNextChapterId(comicId, chapter.getChapterNumber()).orElse(null);

        return chapterMapper.toDetailResponse(chapter, pages, prevChapterId, nextChapterId);
    }

    @Override
    public void create(Long comicId, UploadChapterRequest request) {
        log.info("comic id: {}, \nrequest: {}", comicId, request);
        if (request.files() == null || request.files().isEmpty()) {
            throw new IllegalArgumentException("Danh sách chapter không được để trống");
        }

        Comic comic = comicRepository.findById(comicId).orElseThrow(() ->
                new AppException(ErrorCode.COMIC_NOT_FOUND));

        List<MultipartFile> sortedMultipartFiles = request.files().stream()
                .sorted(Comparator.comparing(f -> f.getOriginalFilename() != null ? f.getOriginalFilename() : ""))
                .toList();
        Chapter chapter = chapterMapper.toEntity(request);
        log.info("chapter: {}", chapter);

        chapter.setComic(comic);
        Chapter saveChapter = chapterRepository.save(chapter);
        String storagePath = String.format("comics/%d/chapters/%d", comicId, saveChapter.getId());
        saveChapter.setStoragePath(storagePath);

        chapterRepository.save(saveChapter);

        Path tempDirPath;
        try {
            tempDirPath = Files.createTempDirectory("chapter_" + saveChapter.getId() + "_");
        } catch (IOException e) {
            saveChapter.setUploadStatus(ChapterUploadStatus.FAILED);
            saveChapter.setErrorMessage("Không thể khởi tạo thư mục tạm");
            chapterRepository.save(saveChapter);
            throw new RuntimeException("Lỗi I/O server", e);
        }

        File tempDir = tempDirPath.toFile();
        List<File> savedTempFiles = new ArrayList<>();

        try {
            for (MultipartFile multipartFile : sortedMultipartFiles) {
                String originalFilename = multipartFile.getOriginalFilename();
                File destFile = tempDirPath.resolve(originalFilename != null ? originalFilename : "page.webp").toFile();

                multipartFile.transferTo(destFile);

                // check magic bytes có phải webp không
                if (!ImageValidator.isWebP(destFile)) {
                    throw new IllegalArgumentException("File " + originalFilename + " không phải định dạng WebP hợp lệ");
                }

                savedTempFiles.add(destFile);
            }
        } catch (Exception e) {
            // xóa dọn dẹp đĩa
            FileSystemUtils.deleteRecursively(tempDir);
            chapter.setUploadStatus(ChapterUploadStatus.FAILED);
            chapter.setErrorMessage(e.getMessage());
            chapterRepository.save(chapter);
            throw new RuntimeException("Lỗi lưu trữ file tạm: " + e.getMessage(), e);
        }

        chapterAsyncService.uploadChapterPagesAsync(saveChapter.getId(), comicId, savedTempFiles, tempDir);
        log.info("Đã ủy quyền upload ngầm cho Chapter ID: {}", chapter.getId());
    }
}
