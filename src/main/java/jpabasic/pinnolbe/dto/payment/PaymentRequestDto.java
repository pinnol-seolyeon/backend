package jpabasic.pinnolbe.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.payment.Payment;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {

    @Schema(description="지불방법", example="EASY_PAY",allowableValues = {"CARD","EASY_PAY"})
    private PayType payType;

    @Schema(description="지불금액",example="1000")
    private Long amount;

    @Schema(description="수량",example="1")
    private Integer quantity;

    @Schema(description = "주문 상품 이름",example="MONTH",allowableValues = {"MONTH","YEAR"})
    private String orderName;

    @Schema(description="구매자 이메일",example="cherry_kang@naver.com")
    private String customerEmail;

    @Schema(description = "구매자 이름",example="강민서")
    private String customerName;

    public Payment toEntity(String userId){
        return Payment.builder()
                .payType(payType)
                .orderId(UUID.randomUUID().toString())
                .amount(amount)
                .quantity(quantity)
                .orderName(orderName)
                .userId(userId)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .build();
    }
}
