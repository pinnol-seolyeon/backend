package jpabasic.pinnolbe.dto.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudyTimeDetailDto {
    private DayOfWeek dayOfWeek;
    private String timeZone;
    private Long minutes;
}
