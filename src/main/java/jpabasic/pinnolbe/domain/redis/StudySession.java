package jpabasic.pinnolbe.domain.redis;

import com.fasterxml.jackson.annotation.JsonFormat;
import jpabasic.pinnolbe.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RedisHash(value="studySession",timeToLive=3600) //1시간 TTL
@NoArgsConstructor
@AllArgsConstructor
public class StudySession {

    @Id
    private String key;
    private String userId;
    private String bookId;
    private String chapterId;
    private int level; //학습 단계

    // ✅ 그냥 LocalDateTime (항상 KST로만 저장)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActive;

//    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Seoul")
//    @Nullable
//    private LocalDateTime lastActive;
    private long idleDuration; //inactive 누적 시간 (분)
    private long totalDuration; //누적 학습 시간 (분)
    private Map<String,Long> timeZoneDurations=new HashMap<>(); //각 학습 시간대 누적 시간 (분)
    private LocalDateTime inactiveSince; //비활성화 시작 시간
    private Status status;

    //특정 레벨 학습 시작 시
    public StudySession(String  key,String userId,String chapterId,int level) {
        this.key=key;
        this.userId = userId;
        this.chapterId=chapterId;
        this.level = level;
        this.startTime = LocalDateTime.now();
        this.status = Status.ACTIVE;
        this.idleDuration= 0;

    }

    public void addIdleDuration(long minutes){
        this.idleDuration+=minutes;
    }

    public void addTotalDuration(long minutes){this.totalDuration+=minutes;}


    //누적 학습 시간대 측정
    public void addDurationToTimeZone(LocalDateTime from,LocalDateTime to){
        if(from.isAfter(to)) return; //잘못된 입력 방지

        LocalDateTime boundary=getNextBoundary(from);
        if(to.isBefore(boundary) || to.equals(boundary)){
            //같은 시간대 안에 머무름
            String zone=classifyTimeZone(from);
            Long diff = (Long) Duration.between(from,to).toMinutes();
            timeZoneDurations.merge(zone,diff, (o, n) -> (Long) (o + n));
        }else{
            //경계를 넘어감 -> 분기 처리
            String zone=classifyTimeZone(from);
            Long firstDiff = (Long) Duration.between(from,to).toMinutes();
            timeZoneDurations.merge(zone,firstDiff, (o, n) -> (Long) (o + n));

            //다음 시간대 구간 재귀 처리
            addDurationToTimeZone(boundary,to);
        }
    }

    //시간대 경계
    private LocalDateTime getNextBoundary(LocalDateTime time){
        int hour=time.getHour();

        LocalDate date=time.toLocalDate();
        if (hour < 6) return date.atTime(6, 0);      // 새벽 → 아침
        else if (hour < 12) return date.atTime(12, 0); // 아침 → 낮
        else if (hour < 18) return date.atTime(18, 0); // 낮 → 저녁
        else return date.plusDays(1).atTime(0, 0);   // 저녁 → 다음날 새벽
    }

    //학습 시간대 분류
    private String classifyTimeZone(LocalDateTime time){
        int hour=time.getHour();
        if(hour>=0 && hour<6) return "DAWN";
        if(hour>=6 && hour<12) return "MORNING";
        if(hour>=12 && hour<18) return "AFTERNOON";
        return "EVENING";
    }
}
