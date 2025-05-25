package jpabasic.pinnolbe.dto.study;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudyStatsDto {
    private int totalCompleted;
    private int weeklyCompleted;

}
