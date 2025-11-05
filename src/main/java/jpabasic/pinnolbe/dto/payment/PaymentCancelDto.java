package jpabasic.pinnolbe.dto.payment;

import jpabasic.pinnolbe.domain.payment.CancelPayment;

public class PaymentCancelDto {

    private String lastTransactionKey;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method; //결제 방법
    private String status;

    public CancelPayment toCancelPayment() {
        return CancelPayment.builder()
                .orderId(orderId)
                .orderName(orderName)
                .paymentKey(paymentKey)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .cardCompany(card.getCompany())
                .cardNumber(card.getNumber())
                .receiptUrl(card.getReceiptUrl())
                .cancelAmount(cancesl[0].getCancelAmount())
                .cancelAmountDate(cancesl[0].getCanceledAt())
                .cancelReason(cancesl[0].getCancelReason())
                .build();
    }
}
