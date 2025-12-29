package jpabasic.pinnolbe.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="cancelPayment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CancelPayment {

    @Id
    private String id;
    private String approvedAt;
    private Long cancelAmount;
    private String cancelDate;
    private String cancelReason;
    private String cardCompany;
    private String cardNumber;
    private String orderId;
    private String paymentKey;
    private String orderName;
    private String receiptUrl;
    private String requestedAt;
    private String userId;
}
