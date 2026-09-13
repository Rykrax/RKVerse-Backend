package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.feature.comic.dto.request.CreateComicRequest;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ComicMapper {
    @Mapping(target = "coverPath", ignore = true)
    @Mapping(target = "status", ignore = true)
    Comic toEntity(CreateComicRequest request);

    ComicResponse toResponse(Comic comic);
}
