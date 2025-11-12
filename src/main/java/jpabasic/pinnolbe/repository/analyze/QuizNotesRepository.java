package jpabasic.pinnolbe.repository.analyze;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuizNotesRepository extends MongoRepository<QuizNotes, String> {
}
