package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.enums.ChapterStatus;
import dev.rykrax.rkverse.feature.comic.Comic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Page<Chapter> findByComicIdAndStatusAndDeletedAtIsNull(
            Long comicId,
            ChapterStatus status,
            Pageable pageable
    );
    Page<Chapter> findByComicId(Long comicId, Pageable pageable);
    Optional<Chapter> findByIdAndStatusAndDeletedAtIsNull(Long id, ChapterStatus status);
    boolean existsByComicIdAndChapterNumber(Long comicId, BigDecimal chapterNumber);

    @Query("""
        SELECT c.id FROM Chapter c 
        WHERE c.comic.id = :comicId 
          AND c.chapterNumber < :currentNumber 
          AND c.status = 'PUBLISHED' 
          AND c.deletedAt IS NULL 
        ORDER BY c.chapterNumber DESC 
        LIMIT 1
    """)
    Optional<Long> findPrevChapterId(@Param("comicId") Long comicId, @Param("currentNumber") BigDecimal currentNumber);

    @Query("""
        SELECT c.id FROM Chapter c 
        WHERE c.comic.id = :comicId 
          AND c.chapterNumber > :currentNumber 
          AND c.status = 'PUBLISHED' 
          AND c.deletedAt IS NULL 
        ORDER BY c.chapterNumber ASC 
        LIMIT 1
    """)
    Optional<Long> findNextChapterId(@Param("comicId") Long comicId, @Param("currentNumber") BigDecimal currentNumber);

    @Query("""
        SELECT c FROM Chapter c
        WHERE c.comic.id = :comicId
        AND c.chapterNumber = :chapterNumber
        AND c.status = :status
    """)
    Optional<Chapter> findActivityChapter(
            @Param("comicId") Long comicId,
            @Param("chapterNumber") BigDecimal chapterNumber,
            @Param("status") ChapterStatus status
    );

    @Query("SELECT c.comic.id FROM Chapter c WHERE c.id = :chapterId")
    Optional<Long> findComicIdByChapterId(@Param("chapterId") Long chapterId);

    @Query("SELECT c.comic FROM Chapter c WHERE c.id = :chapterId")
    Optional<Comic> findComicByChapterId(@Param("chapterId") Long chapterId);

    @Modifying
    @Query("UPDATE Chapter c SET c.views = c.views + :increment WHERE c.id = :chapterId")
    void incrementViewCount(@Param("chapterId") Long chapterId, @Param("increment") Long increment);
}
