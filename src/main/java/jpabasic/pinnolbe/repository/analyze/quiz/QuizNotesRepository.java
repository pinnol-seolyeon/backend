package jpabasic.pinnolbe.repository.analyze.quiz;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import jpabasic.pinnolbe.dto.quiz.QuizType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizNotesRepository extends MongoRepository<QuizNotes, String> {

    Optional<QuizNotes> findByUserIdAndChapterIdAndQuizType(String userId, String chapterId, QuizType quizType);
}
