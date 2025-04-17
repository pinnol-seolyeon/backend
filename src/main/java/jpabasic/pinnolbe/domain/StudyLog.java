package jpabasic.pinnolbe.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "study_logs")
@Getter
@Setter
@NoArgsConstructor
public class StudyLog {
    @Id
    private String id;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

