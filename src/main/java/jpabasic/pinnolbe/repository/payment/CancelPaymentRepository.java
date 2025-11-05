package jpabasic.pinnolbe.repository.payment;

import jpabasic.pinnolbe.domain.payment.CancelPayment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CancelPaymentRepository extends MongoRepository<CancelPayment, String> {
    Optional<CancelPayment> findByPaymentKey(String orderId);
}
