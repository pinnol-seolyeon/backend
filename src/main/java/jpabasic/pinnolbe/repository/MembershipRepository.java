package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Membership;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends MongoRepository<Membership,String> {
    /**
     * 특정 유저의 멤버십 중 종료일(endDate)이 가장 늦은 데이터 1건을 조회합니다.
     */
    Optional<Membership> findTopByUserIdOrderByEndDateDesc(String userId);

    List<Membership> findAllByUserIdAndActiveTrue(String userId);
}


