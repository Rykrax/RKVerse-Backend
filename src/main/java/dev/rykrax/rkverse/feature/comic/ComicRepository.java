package dev.rykrax.rkverse.feature.comic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComicRepository extends JpaRepository<Comic, Long> {
    @Modifying
    @Query("UPDATE Comic c SET c.views = c.views + :increment WHERE c.id = :comicId")
    void incrementViewCount(@Param("comicId") Long comicId, @Param("increment") Long increment);
}
