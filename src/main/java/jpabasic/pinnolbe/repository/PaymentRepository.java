package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Payment;
import jpabasic.pinnolbe.domain.study.Content;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
}
