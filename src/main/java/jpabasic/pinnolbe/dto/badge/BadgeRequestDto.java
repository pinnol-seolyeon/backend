package jpabasic.pinnolbe.dto.badge;

import io.swagger.v3.oas.annotations.media.Schema;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BadgeRequestDto {

    private String chapterId;
    private List<BadgeType> badgeType; //스피드 사냥꾼, 정교한 사냥꾼

    public BadgeRequestDto(String chapterId, BadgeType badgeType) {
        this.chapterId = chapterId;
        this.badgeType = List.of(badgeType);
    }
}
