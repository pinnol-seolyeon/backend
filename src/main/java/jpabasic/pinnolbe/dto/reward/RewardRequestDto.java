package jpabasic.pinnolbe.dto.reward;

import jpabasic.pinnolbe.domain.reward.PointCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardRequestDto {

    private Integer coin;
    private PointCategory category;
    private String chapterId;
    private boolean isPositive; //양수=true, 음수=false

}
