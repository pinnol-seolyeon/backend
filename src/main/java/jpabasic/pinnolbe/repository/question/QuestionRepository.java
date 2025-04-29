package jpabasic.pinnolbe.repository.question;

import jpabasic.pinnolbe.dto.question.QuestionAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends MongoRepository<QuestionAnswer, String> {
}
