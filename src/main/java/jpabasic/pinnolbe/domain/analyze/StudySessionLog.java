package jpabasic.pinnolbe.domain.analyze;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.StudySession;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection="studySessionLog")
@Getter
@Setter
@NoArgsConstructor
public class StudySessionLog {

    @Id
    private String id;
    private String userId;
    private String chapterId;
    private int level;
    private long totalDuration; //누적 학습 시간 (분)
    private Map<String,Long> timeZoneDurations; //각 학습 시간대 누적 시간 (분)
    private Status status;

    public StudySessionLog(
            String userId,String chapterId,int level
    ) {
        this.userId=userId;
        this.chapterId=chapterId;
        this.level=level;
        this.totalDuration=0;
    }

}
