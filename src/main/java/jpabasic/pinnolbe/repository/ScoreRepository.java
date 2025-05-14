package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Score;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends MongoRepository<Score, Long> {}

