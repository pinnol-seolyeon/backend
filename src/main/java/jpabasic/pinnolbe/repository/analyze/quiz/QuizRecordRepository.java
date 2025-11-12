package jpabasic.pinnolbe.repository.analyze.quiz;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRecordRepository extends MongoRepository<QuizRecord, String> {
    List<QuizRecord> findAllByQuizNotesId(String quizNotesId);
}
