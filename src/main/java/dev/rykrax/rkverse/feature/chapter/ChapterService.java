package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.common.RedisService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
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
    private final RedisService redisService;
    @Value("${cloudflare.r2.public-domain}")
    private String publicDomain;
    private final ObjectMapper objectMapper;

    private static final String CHAPTER_CACHE_PREFIX = "chapter:detail:";
    private static final Duration CHAPTER_CACHE_TTL = Duration.ofDays(3);

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
    public ChapterDetailResponse getChapterDetail(Long comicId, BigDecimal chapterNumber) {
        String cacheKey = CHAPTER_CACHE_PREFIX + comicId + ":" + chapterNumber;
        try {
            String cachedJson = redisService.get(cacheKey);
            if (cachedJson != null && !cachedJson.isBlank()) {
                log.info("==> [Cache HIT] Lấy dữ liệu Chapter từ Redis: {}", cacheKey);
                return objectMapper.readValue(cachedJson, ChapterDetailResponse.class);
            }
        } catch (Exception e) {
            log.warn("==> [Cache ERROR] Lỗi đọc Redis Cache cho key {}, fallback query Database: {}", cacheKey, e.getMessage());
        }

        log.info("==> [Cache MISS] Truy vấn MySQL cho Comic ID: {}, Chapter: {}", comicId, chapterNumber);

        Chapter chapter = chapterRepository
                .findActivityChapter(comicId, chapterNumber, ChapterStatus.PUBLISHED)
                .orElseThrow(() -> new AppException(ErrorCode.COMIC_NOT_FOUND));

        String cleanDomain = publicDomain.endsWith("/")
                ? publicDomain.substring(0, publicDomain.length()-1)
                : publicDomain;

        String basePath = (chapter.getStoragePath() != null && !chapter.getStoragePath().isBlank())
                ? chapter.getStoragePath()
                : String.format("comics/%d/chapters/%d", comicId, chapter.getId());

        int totalPages = chapter.getTotalPages() != null ? chapter.getTotalPages() : 0;
        List<String> pages = IntStream.rangeClosed(1, totalPages)
                .mapToObj(i -> String.format("%s/%s/%03d.webp", cleanDomain, basePath, i))
                .toList();

        Long prevChapterId = chapterRepository.findPrevChapterId(comicId, chapter.getChapterNumber()).orElse(null);
        Long nextChapterId = chapterRepository.findNextChapterId(comicId, chapter.getChapterNumber()).orElse(null);

        ChapterDetailResponse response = chapterMapper.toDetailResponse(chapter, pages, prevChapterId, nextChapterId);
        try {
            String jsonPayload = objectMapper.writeValueAsString(response);
            redisService.set(cacheKey, jsonPayload, CHAPTER_CACHE_TTL);
        } catch (Exception e) {
            log.warn("==> Không thể ghi cache vào Redis cho key {}: {}", cacheKey, e.getMessage());
        }
        return response;
    }

    @Override
    public void create(Long comicId, UploadChapterRequest request) {
        log.info("comic id: {}, \nrequest: {}", comicId, request);
        if (request.files() == null || request.files().isEmpty()) {
            throw new IllegalArgumentException("Danh sách chapter không được để trống");
        }

        Comic comic = comicRepository.findById(comicId).orElseThrow(() ->
                new AppException(ErrorCode.COMIC_NOT_FOUND));

        if (chapterRepository.existsByComicIdAndChapterNumber(comicId, request.chapterNumber())) {
            throw new AppException(ErrorCode.CHAPTER_ALREADY_EXISTS);
        }

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
