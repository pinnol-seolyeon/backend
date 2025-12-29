package jpabasic.pinnolbe.repository.payment;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import jpabasic.pinnolbe.domain.payment.OrdererProfile;
import jpabasic.pinnolbe.domain.payment.Payment;

public interface OrdererProfileRepository extends MongoRepository<OrdererProfile, String> {
	Optional<OrdererProfile> findByUserId(String userId);
}
