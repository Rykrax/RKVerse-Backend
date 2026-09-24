package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.common.RedisService;
import dev.rykrax.rkverse.feature.comic.ComicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewBatchSyncScheduler {

    private final RedisService redisService;
    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;

    private static final String CHAPTER_VIEW_HASH = "chapter:views";
    private static final String COMIC_VIEW_HASH = "comic:views";

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void syncAllViewsToDatabase() {
        syncHashViews(CHAPTER_VIEW_HASH, "chapter:views:sync:", chapterRepository::incrementViewCount
        );

        syncHashViews(COMIC_VIEW_HASH, "comic:views:sync:", comicRepository::incrementViewCount
        );
    }

    private void syncHashViews(String sourceKey, String syncPrefix, java.util.function.BiConsumer<Long, Long> dbUpdater) {
        String tempKey = syncPrefix + System.currentTimeMillis();

        if (!redisService.renameSafely(sourceKey, tempKey)) {
            return;
        }

        redisService.expire(tempKey, Duration.ofMinutes(15));

        Map<Object, Object> viewsData = redisService.getHashEntries(tempKey);
        if (viewsData.isEmpty()) {
            redisService.delete(tempKey);
            return;
        }

        log.info("Đang đồng bộ {} bản ghi từ [{}] xuống Database...", viewsData.size(), sourceKey);
        viewsData.forEach((k, v) -> {
            Long id = Long.parseLong(k.toString());
            Long increment = Long.parseLong(v.toString());
            dbUpdater.accept(id, increment);
        });

        redisService.delete(tempKey);
        log.info("Đồng bộ [{}] xuống Database thành công.", sourceKey);
    }
}