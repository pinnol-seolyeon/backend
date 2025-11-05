package jpabasic.pinnolbe.repository.payment;

import jpabasic.pinnolbe.domain.payment.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);

}
