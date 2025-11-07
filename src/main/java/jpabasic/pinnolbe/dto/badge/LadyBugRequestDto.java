package jpabasic.pinnolbe.dto.badge;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LadyBugRequestDto {

    private String chapterId;
    @Schema(
            description="배지 타입(스피드 사냥꾼, 정교한 사냥꾼 중 선택)",
            allowableValues = {"SPEED_HUNTER","FINE_HUNTER"})
    private BadgeType badgeType; //스피드 사냥꾼, 정교한 사냥꾼

}
