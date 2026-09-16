package dev.rykrax.rkverse.feature.chapter;

import dev.rykrax.rkverse.feature.chapter.dto.request.UploadChapterRequest;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterDetailResponse;
import dev.rykrax.rkverse.feature.chapter.dto.response.ChapterResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChapterMapper {
    ChapterResponse toResponse(Chapter chapter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalPages", ignore = true)
    @Mapping(target = "views", ignore = true)
    Chapter toEntity(UploadChapterRequest request);

    @Mapping(source = "chapter.comic.id", target = "comicId")
    @Mapping(source = "chapter.id", target = "id")
    @Mapping(source = "chapter.title", target = "title")
    @Mapping(source = "chapter.chapterNumber", target = "chapterNumber")
    @Mapping(source = "chapter.totalPages", target = "totalPages")
    @Mapping(source = "pages", target = "pages")
    @Mapping(source = "prevChapterId", target = "prevChapterId")
    @Mapping(source = "nextChapterId", target = "nextChapterId")
    ChapterDetailResponse toDetailResponse(
            Chapter chapter,
            List<String> pages,
            Long prevChapterId,
            Long nextChapterId
    );
}
