package jpabasic.pinnolbe.dto.payment;

import lombok.Data;

@Data
public class PaymentResHandleCardDto {

    private String status;        // DONE
    private String orderId;
    private String paymentKey;
    private String approvedAt;
    private String method;

    private Card card;

    @Data
    public static class Card {
        private String company;
        private String number;
        private Integer installmentPlanMonths;
        private Boolean isInterestFree;
        private String approveNo;
        private Boolean useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private String receiptUrl;
    }
}

