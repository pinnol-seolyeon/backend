package jpabasic.pinnolbe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;


@Builder
@Schema(description = "마이페이지 내 이용권 정보 조회 응답 스키마")
public record UserMembershipSummaryResponse (
        @Schema(description = "이용권 이름", example = "1개월 권 (연장)")
        String membershipName,

        @Schema(description = "이용 기간 (시작일 ~ 종료일)", example = "2026.03.28 ~ 2026.04.28")
        String usagePeriod,

        @Schema(description = "총 이용 기간 텍스트", example = "1개월")
        String totalMonthsText,

        @Schema(description = "현재 홀딩(일시정지) 중 여부", example = "true")
        boolean isHolding,

        @Schema(description = "남은 홀딩 일수 (홀딩 중이 아닐 경우 0)", example = "7")
        long remainingHoldDays
){

}
