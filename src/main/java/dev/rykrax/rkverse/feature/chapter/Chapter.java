package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.feature.comic.Comic;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_number", nullable = false, precision = 6, scale = 1)
    private BigDecimal chapterNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "total_pages")
    private Integer totalPages = 0;

    @Column(name = "views")
    private Long views = 0L;
//    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderBy("pageNumber ASC")
//    @Builder.Default
//    private List<ChapterPage> pages = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;

//    public void addPage(ChapterPage page) {
//        pages.add(page);
//        page.setChapter(this);
//    }

}
