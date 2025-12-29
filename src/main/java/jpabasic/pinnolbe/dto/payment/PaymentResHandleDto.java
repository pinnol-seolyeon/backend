package jpabasic.pinnolbe.dto.payment;

import lombok.Data;

@Data
public class PaymentResHandleDto {
    String paymentKey;
    String orderId;
    String orderName;
    String method;
    String totalAmount;
    String status; //결제 처리 상태
    String requestAt; //2021-01-01T10:01:30+09:00
    String approvedAt; //2021-01-01T10:01:30+09:00
    PaymentResHandleCardDto card; //카드 결제
//    PaymentResHandleCancleDto cancels; //결제 취소 이력 관련 객체
    String type; //NORMAL, 결제 타입 저오(NORMAL, BILLING, CONNECTPAY)
}
