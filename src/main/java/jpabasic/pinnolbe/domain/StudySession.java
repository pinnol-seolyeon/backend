package jpabasic.pinnolbe.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.sql.Time;
import java.time.LocalDateTime;

@Getter
@Setter
@RedisHash(value="studySession",timeToLive=3600) //1시간 TTL
public class StudySession {

    @Id
    private String key;
    private String userId;
    private String chapterId;
    private int level; //학습 단계

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Seoul")
    private LocalDateTime startTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Seoul")
    private LocalDateTime endTime;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Seoul")
    private LocalDateTime lastActive;
    private long idleDuration;
    private Status status;

    //특정 레벨 학습 시작 시
    public StudySession(String userId,int level) {
        this.userId = userId;
        this.level = level;
        this.startTime = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
        this.status = Status.ACTIVE;
        this.idleDuration= 0;
    }

    public void addIdleDuration(long minutes){
        this.idleDuration+=minutes;
    }
}
