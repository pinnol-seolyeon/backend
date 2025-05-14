package jpabasic.pinnolbe.dto;

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

