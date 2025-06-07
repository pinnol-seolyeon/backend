package jpabasic.pinnolbe.dto.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodayStudyTypeResponse {
    private int hours;
    private int minutes;
    private String studyType;
}

