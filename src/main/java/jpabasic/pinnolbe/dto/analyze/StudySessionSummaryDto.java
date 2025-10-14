package jpabasic.pinnolbe.dto.analyze;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description="사용자의 학습 세션 중간 점검 요약 정보 DTO")
public class StudySessionSummaryDto {

    private String userId;
    private String chapterId;
    @Schema(description = "현재 학습 하고 있는 level")
    private int level;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description="해당 레벨 학습 시작 시간(ISO 8601 형식) /start api에서만 신경써주면 됩니다.", example = "2025-10-10T19:20:00")
    private LocalDateTime startTime;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description="해당 레벨 학습 시작 시간(ISO 8601 형식) /update api에서만 신경써주면 됩니다.", example = "2025-10-10T19:20:00")
    private LocalDateTime lastActive;

//    private OffsetDateTime startTime;
//    private OffsetDateTime lastActive;

    @Schema(description="학습 상태 - INACTIVE = 유저가 5분 이상 움직이지 않을 때")
    private Status status;
}
