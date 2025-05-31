package jpabasic.pinnolbe.dto.analyze;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RadarScoreComparisonDto {
    private RadarScoreDto thisWeek;
    private RadarScoreDto lastWeek;
}
