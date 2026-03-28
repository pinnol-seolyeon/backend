package jpabasic.pinnolbe.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import lombok.Builder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Builder
@Schema(description = "홀딩할 날짜를 입력받습니다.")
public record HoldingRequest (

        @Schema(description = "홀딩 시작일자",example = "2026-03-01")
        LocalDate startDate,
        @Schema(description = "홀딩 마지막일자",example = "2026-03-03")
        LocalDate endDate

){
    public static HoldingRequest of(LocalDate start,LocalDate end){
        if(start.isAfter(end)) throw new CustomException(ErrorCode.INVALID_HOLD_PERIOD);
        return new HoldingRequest(start,end);
    }
}
