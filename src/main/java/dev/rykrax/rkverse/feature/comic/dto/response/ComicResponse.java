package dev.rykrax.rkverse.feature.comic.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComicResponse(
        Long id,
        String title,
        String slug,
        String description,
        String author,
        String artist,
        String coverPath,
        String bannerPath
) {
}
