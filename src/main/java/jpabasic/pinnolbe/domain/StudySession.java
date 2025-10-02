package jpabasic.pinnolbe.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.sql.Time;
import java.time.LocalDateTime;

@Getter
@RedisHash(value="studySession", timeToLive=60)
public class StudySession {

    @Id
    private String userId;
    private String chapterId;
    private int level; //학습 단계
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastActive;
    private Time idleDuration;
    private Status status;

    //특정 레벨 학습 시작 시
    public StudySession(String userId,int level) {
        this.userId = userId;
        this.level = level;
        this.startTime = LocalDateTime.now();
    }
}
