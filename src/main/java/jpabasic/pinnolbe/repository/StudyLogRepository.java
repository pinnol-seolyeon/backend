package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.StudyLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StudyLogRepository extends MongoRepository<StudyLog, String> {
    List<StudyLog> findByUserIdAndStartTimeBetween(String userId, LocalDateTime start, LocalDateTime end);

    List<StudyLog> findByUserId(String userId);

    List<StudyLog> findByUserIdAndDate(String userId, LocalDate date);


}

