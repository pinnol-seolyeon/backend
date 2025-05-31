package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.study.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByChapterId(String chapterId);
}

