package jpabasic.pinnolbe.dto.analyze;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RadarScoreDto {
    private double engagement;     // 0 ~ 1
    private double focus;          // 0 ~ 1
    private double understanding;  // 0 ~ 1
    private double expression;     // 0 ~ 1
}