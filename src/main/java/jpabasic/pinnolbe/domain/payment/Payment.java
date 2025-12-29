package jpabasic.pinnolbe.domain.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.BaseEntity;
import jpabasic.pinnolbe.dto.payment.PayType;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection="payment")
@Schema(description="결제 정보 엔티티")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    private String id;
    private PayType payType;
    private Long amount;
    private Integer quantity;
    private String orderId;
    private String orderName;
    private String status; //DONE, CANCELED
    private String method; //CARD, EASY_PAY
    private String approvedAt;
    private String paySuccessYn;

    //관리자 페이지 전용 정보
    // private String cardCompany;
    // private String approveNo;
    // private String cardType;
    // private String ownerType;

    //유저 관련 필드
    private String userId;
    private String customerEmail;
    private String customerName;

    //결제 실패 시 정보
    private String payFailReason;
    private String paymentKey;

    public PaymentResponseDto toDto(){
        return PaymentResponseDto.builder()
                .payType(payType.name())
                .amount(amount)
                .quantity(quantity)
                .orderId(orderId)
                .orderName(orderName)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .status(status)
                .method(method)
                .build();
    }
}
