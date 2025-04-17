package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.QuizResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizResultRepository extends MongoRepository<QuizResult, String> {
    List<QuizResult> findByUserIdOrderByDateAsc(String userId);
}

