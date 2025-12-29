package jpabasic.pinnolbe.dto.payment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private String payType; //지불 방법
    private Long amount; //지불 금액
    private Integer quantity; //수량
    private String orderId; //주문 고유 ID
    private String orderName; //주문 상품 이름


    private String customerEmail; //구매자 이메일
    private String customerName; //구매자 이름


    private String approvedAt;
    private String paySuccessYn; //결제 성공 여부
    private String status; //DONE, CANCELED
    private String method;

}