package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
    boolean existsByCode(String code);
}
