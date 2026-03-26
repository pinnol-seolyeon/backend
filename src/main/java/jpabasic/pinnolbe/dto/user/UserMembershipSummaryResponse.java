package jpabasic.pinnolbe.dto.user;

import lombok.Builder;
import lombok.Getter;


@Builder
public record UserMembershipSummaryResponse (
        String ticketName,
        int totalQuantity,
        String usagePeriod,
        String totalMonthsText
){

}
