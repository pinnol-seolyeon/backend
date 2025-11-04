package jpabasic.pinnolbe.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.dto.payment.PayType;
import jpabasic.pinnolbe.dto.payment.PaymentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="payment")
@Schema(description="결제 정보 엔티티")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment{
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

    private String createDate;

    public PaymentResponseDto toDto(String paySuccessYn){
        return PaymentResponseDto.builder()
                .payType(payType.name())
                .amount(amount)
                .orderId(orderId)
                .orderName(orderName)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .createDate(createDate)
                .paySuccessYn(paySuccessYn)
                .build();
    }
}
