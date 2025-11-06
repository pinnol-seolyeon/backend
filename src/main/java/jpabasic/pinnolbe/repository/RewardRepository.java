package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.reward.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends MongoRepository<Reward, String> {

    Page<Reward> findAllByUserId(String userId, Pageable pageable);
}
