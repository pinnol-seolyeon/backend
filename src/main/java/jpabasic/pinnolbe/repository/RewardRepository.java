package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Reward;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends MongoRepository<Reward, String> {

    Reward findByUserId(String userId);
}
