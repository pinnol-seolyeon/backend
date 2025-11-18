package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.ChapterProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChapterProgressRepository extends MongoRepository<ChapterProgress, String> {
    Optional<ChapterProgress> findByUserIdAndChapterId(String userId, String chapterId);

    List<ChapterProgress> findByUserId(String userId);
}
