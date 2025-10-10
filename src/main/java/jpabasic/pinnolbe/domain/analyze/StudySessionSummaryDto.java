package jpabasic.pinnolbe.domain.analyze;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Null;
import jpabasic.pinnolbe.domain.Status;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
public class StudySessionSummaryDto {

    private String userId;
    private String chapterId;
    private int level;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActive;

//    private OffsetDateTime startTime;
//    private OffsetDateTime lastActive;

    private Status status;
}
