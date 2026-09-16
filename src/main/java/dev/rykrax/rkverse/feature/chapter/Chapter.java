package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.enums.ChapterStatus;
import dev.rykrax.rkverse.enums.ChapterUploadStatus;
import dev.rykrax.rkverse.feature.comic.Comic;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "chapters", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"comic_id", "chapter_number"})
})
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_number", nullable = false, precision = 6, scale = 1)
    private BigDecimal chapterNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ChapterStatus status = ChapterStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status")
    @Builder.Default
    private ChapterUploadStatus uploadStatus = ChapterUploadStatus.PENDING;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "storage_path")
    private String storagePath;

    @Column(name = "total_pages")
    @Builder.Default
    private Integer totalPages = 0;

    @Column(name = "views")
    @Builder.Default
    private Long views = 0L;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;
}
