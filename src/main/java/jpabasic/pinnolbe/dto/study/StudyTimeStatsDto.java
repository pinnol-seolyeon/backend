package jpabasic.pinnolbe.dto.study;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StudyTimeStatsDto {
    private String preferredType; // 예: "아침형"
    private Map<String, Map<String, Integer>> weeklyStats;
}