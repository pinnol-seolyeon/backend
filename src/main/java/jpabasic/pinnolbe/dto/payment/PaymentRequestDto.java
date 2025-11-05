package jpabasic.pinnolbe.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.payment.Payment;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    @Schema(description="지불방법", example="CARD",allowableValues = {"CARD"})
    private PayType payType;
    @Schema(description="지불금액")
    private Long amount;
    @Schema(description = "주문 상품 이름",example="MONTH",allowableValues = {"MONTH","YEAR"})
    private String orderName;
    @Schema(description="구매자 이메일")
    private String customerEmail;
    @Schema(description = "구매자 이름")
    private String customerName;

    public Payment toEntity(){
        return Payment.builder()
                .orderId(UUID.randomUUID().toString())
                .payType(payType)
                .amount(amount)
                .orderName(orderName)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .paySuccessYn("Y")
                .createDate(LocalDate.now().toString())
                .build();
    }
}
