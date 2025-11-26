package jpabasic.pinnolbe.dto.payment;

import lombok.Data;

@Data
public class PaymentResHandleCardDto {
    String company;
    String number;
    String installmentPlanMonths;
    String isInterestFree;
    String approveNo;
    String useCardPoint;
    String cardType;
    String ownerType;
    String acquireStatus;
    String receiptUrl;
}
