package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.Payment;
import jpabasic.pinnolbe.domain.study.Content;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
