CREATE INDEX idx_chapters_comic_chapter_status_deleted
    ON chapters (
                 comic_id,
                 chapter_number,
                 status,
                 deleted_at
        );
