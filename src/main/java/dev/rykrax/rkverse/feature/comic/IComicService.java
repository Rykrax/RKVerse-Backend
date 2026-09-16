package dev.rykrax.rkverse.feature.comic;

import dev.rykrax.rkverse.common.PageResponse;
import dev.rykrax.rkverse.feature.comic.dto.request.CreateComicRequest;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicDetailResponse;
import dev.rykrax.rkverse.feature.comic.dto.response.ComicResponse;
import org.springframework.data.domain.Pageable;

public interface IComicService {
    PageResponse<ComicResponse> getComics(Pageable pageable);
    ComicDetailResponse getComic(Long id);
    ComicResponse createComic(CreateComicRequest request);
    void delete(Long id);
}
