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
    /**
     * 현재 활성화된 멤버십 조회
     */
    Optional<Membership> findByUserIdAndActiveTrue(String userId);

    /**
     * 특정 유저의 모든 멤버십 이력을 종료일(endDate) 내림차순으로 조회합니다.
     * (최신 이용권이 리스트의 가장 앞에 옵니다.)
     */
    List<Membership> findAllByUserIdOrderByEndDateDesc(String userId);

}


