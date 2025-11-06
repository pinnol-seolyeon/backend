package jpabasic.pinnolbe.dto.analyze;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudySessionLogResponseDto {

    private String id;
    private String userId;
    private String chapterId;
    private String weeklyAnalysisId;
    private int level;
    private long totalDuration;
    private Map<String,Long> timeZoneDurations;
    private LocalDateTime createdAt;


    public static StudySessionLogResponseDto toStudySessionLog(StudySessionLog studySessionLog) {
        StudySessionLogResponseDto dto=new StudySessionLogResponseDto();
        dto.setId(studySessionLog.getId());
        dto.setUserId(studySessionLog.getUserId());
        dto.setChapterId(studySessionLog.getChapterId());
        dto.setLevel(studySessionLog.getLevel());
        dto.setTotalDuration(studySessionLog.getTotalDuration());
        dto.setTimeZoneDurations(new HashMap<>(studySessionLog.getTimeZoneDurations()));
        dto.setCreatedAt(studySessionLog.getCreatedAt());
        return dto;
    }

}
