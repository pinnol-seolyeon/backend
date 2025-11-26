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
    private String orderId;
    private String orderName;
    private String paySuccessYn;

    private String userId;
    private String customerEmail;
    private String customerName;

    private String payFailReason;
    private String paymentKey;

    public PaymentResponseDto toDto(String paySuccessYn){
        return PaymentResponseDto.builder()
                .payType(payType.name())
                .amount(amount)
                .orderId(orderId)
                .orderName(orderName)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .createDate(LocalDateTime.now().toString())
                .paySuccessYn(paySuccessYn)
                .build();
    }
}
