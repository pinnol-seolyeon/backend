package jpabasic.pinnolbe.repository.analyze.quiz;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizNotesRepository extends MongoRepository<QuizNotes, String> {

    Optional<QuizNotes> findByUserIdAndChapterId(String userId, String chapterId);
}
