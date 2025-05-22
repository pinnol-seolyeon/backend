package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Score;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScoreRepository extends MongoRepository<Score, String> {
    List<Score> findByUserId(String userId);
}
