package jpabasic.pinnolbe.dto.analyze;

import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudySessionLogResponseDto {

    private String id;
    private String userId;
    private String chapterId;
    private int level;
    private long totalDuration;
    private Map<String,Long> timeZoneDurations;
    private LocalDateTime createdAt;

    public StudySessionLogResponseDto toDto(StudySessionLog studySessionLog) {
        StudySessionLogResponseDto dto=new StudySessionLogResponseDto();
        dto.setId(studySessionLog.getId());
        dto.setTotalDuration(studySessionLog.getTotalDuration());
        dto.setTimeZoneDurations(studySessionLog.getTimeZoneDurations());
        dto.setCreatedAt(studySessionLog.getCreatedAt());
        return dto;
    }

}
