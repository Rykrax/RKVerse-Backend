package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.RedisService;
import dev.rykrax.rkverse.enums.ErrorCode;
import dev.rykrax.rkverse.exception.AppException;
import dev.rykrax.rkverse.feature.chapter.dto.response.ViewStartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewService {

    private final RedisService redisService;
    private final ChapterRepository chapterRepository;

    private static final String COOLDOWN_PREFIX = "chapter:view:cooldown:";
    private static final String SESSION_PREFIX = "chapter:view:session:";
    private static final String CHAPTER_VIEW_HASH = "chapter:views";
    private static final String COMIC_VIEW_HASH = "comic:views";

    private static final long MIN_READ_SECONDS = 15;
    private static final Duration COOLDOWN_DURATION = Duration.ofMinutes(5);
    private static final Duration SESSION_DURATION = Duration.ofSeconds(60);

    public ViewStartResponse startChapterReading(Long chapterId, String clientKey) {
        String cooldownKey = COOLDOWN_PREFIX + chapterId + ":" + clientKey;

        if (Boolean.TRUE.equals(redisService.hasKey(cooldownKey))) {
            throw new AppException(ErrorCode.SOMETHING_WRONG);
        }

        String readToken = UUID.randomUUID().toString();
        String sessionKey = SESSION_PREFIX + readToken;
        long now = System.currentTimeMillis();

        redisService.set(sessionKey, String.valueOf(now), SESSION_DURATION);
        return new ViewStartResponse(true, readToken);
    }

    public void confirmChapterReading(Long chapterId, String clientKey, String readToken) {
        if (readToken == null || readToken.isBlank()) {
            return;
        }

        String sessionKey = SESSION_PREFIX + readToken;
        String startTimeStr = redisService.get(sessionKey);

        if (startTimeStr == null) {
            log.warn("Confirm view thất bại: Token {} không tồn tại hoặc đã hết hạn", readToken);
            return;
        }

        long duration = (System.currentTimeMillis() - Long.parseLong(startTimeStr)) / 1000;
        if (duration < MIN_READ_SECONDS) {
            log.warn("Confirm view thất bại: Thời gian đọc mới đạt {}s (< 15s)", duration);
            return;
        }

        String cooldownKey = COOLDOWN_PREFIX + chapterId + ":" + clientKey;
        Boolean lockAcquired = redisService.setIfAbsent(cooldownKey, "1", COOLDOWN_DURATION);

        if (!Boolean.TRUE.equals(lockAcquired)) {
            throw new AppException(ErrorCode.WRONG_TOKEN);
        }
            Long comicId = chapterRepository.findComicIdByChapterId(chapterId).orElse(null);

            redisService.hashIncrement(CHAPTER_VIEW_HASH, chapterId.toString(), 1);
            if (comicId != null) {
                redisService.hashIncrement(COMIC_VIEW_HASH, comicId.toString(), 1);
            }
            log.info("Ghi nhận view thành công: Chapter ID {} | Comic ID {}", chapterId, comicId);

        redisService.delete(sessionKey);
    }
}