package jpabasic.pinnolbe.repository.analyze;

import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionLogRepository extends MongoRepository<StudySessionLog, String> {
    Optional<StudySessionLog> findByUserIdAndChapterIdAndLevel(String userId,String chapterId, int level);
}
