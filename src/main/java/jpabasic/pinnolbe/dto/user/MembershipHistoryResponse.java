package jpabasic.pinnolbe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.HoldingPeriod;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "멤버십 이용권 상세 이력 응답")
public class MembershipHistoryResponse {
    @Schema(description = "멤버십 고유 ID")
    private String membershipId;

    @Schema(description = "이용권 명칭", example = "1개월 권")
    private String title;

    @Schema(description = "전체 이용 기간", example = "2026.01.01 ~ 2026.03.15")
    private String period;

    @Schema(description = "현재 상태", example = "사용 중 / 만료 / 홀딩 중")
    private String status;

    @Schema(description = "총 연장된 일수 (홀딩으로 인해)", example = "14일")
    private String totalExtendedDays;

    @Schema(description = "해당 이용권 내 홀딩 상세 내역")
    private List<HoldingPeriod> holdings;

    @Getter
    @Builder
    public static class HoldingHistoryDto {
        private String holdPeriod; // "2026.02.01 ~ 2026.02.07"
        private long days;         // 7
        private String createdAt;  // "2026.01.15 신청"
    }
}
