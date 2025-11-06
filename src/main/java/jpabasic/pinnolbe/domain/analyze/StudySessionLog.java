package jpabasic.pinnolbe.domain.analyze;

import jpabasic.pinnolbe.domain.BaseEntity;
import jpabasic.pinnolbe.domain.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection="studySessionLog")
@Getter
@Setter
@NoArgsConstructor
//학습자의 현 진도 저장
public class StudySessionLog extends BaseEntity {

    @Id
    private String id;
    private String userId;
    private String bookId;
    private String chapterId;
    private int level;
    private long totalDuration; //누적 학습 시간 (분)
    private Map<String,Long> timeZoneDurations= new HashMap<>(); //각 학습 시간대 누적 시간 (분)
    private Status status;

    public StudySessionLog(
            String userId,String chapterId,String bookId,int level
    ) {
        this.userId=userId;
        this.chapterId=chapterId;
        this.bookId=bookId;
        this.level=level;
        this.totalDuration=0;
        this.timeZoneDurations=new HashMap<>();
    }



}
