package jpabasic.pinnolbe.domain.analyze;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="studyTime_logs")
@Getter
@NoArgsConstructor
public class StudyRealTimeLog {

    @Id
    private String id;
    private String userId;
    private String chapterId;
    private int level;

}
