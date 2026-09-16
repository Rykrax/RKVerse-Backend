package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.enums.ComicStatus;
import dev.rykrax.rkverse.enums.ComicUploadStatus;
import dev.rykrax.rkverse.feature.chapter.Chapter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comics")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Comic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Lob
    @Column(name = "alternative_titles", columnDefinition = "TEXT")
    private String alternativeTitles;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "author", length = 150)
    private String author;

    @Column(name = "artist", length = 150)
    private String artist;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ComicStatus status = ComicStatus.ONGOING;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    @Builder.Default
    private ComicUploadStatus uploadStatus = ComicUploadStatus.PENDING;

    @Column(name = "cover_path", length = 255)
    private String coverPath;

    @Column(name = "banner_path", length = 255)
    private String bannerPath;

    @Column(name = "views")
    @Builder.Default
    private Long views = 0L;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chapterNumber ASC")
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();
}
