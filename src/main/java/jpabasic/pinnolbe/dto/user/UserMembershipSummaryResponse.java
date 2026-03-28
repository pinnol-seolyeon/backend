package jpabasic.pinnolbe.dto.user;

import lombok.Builder;


@Builder
public record UserMembershipSummaryResponse (
        String membershipName,
        int totalQuantity,
        String usagePeriod,
        String totalMonthsText
){

}
