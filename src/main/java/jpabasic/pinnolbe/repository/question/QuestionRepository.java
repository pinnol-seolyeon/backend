package jpabasic.pinnolbe.repository.question;

import jpabasic.pinnolbe.dto.question.QuestionSessionDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface QuestionRepository extends MongoRepository<QuestionSessionDto, String> {
}
