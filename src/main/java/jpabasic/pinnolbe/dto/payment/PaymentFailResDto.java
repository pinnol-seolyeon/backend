package jpabasic.pinnolbe.dto.payment;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailResDto {
    String errorCode;
    String errorMsg;
    String orderId;
}
