package jpabasic.pinnolbe.scheduler;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.service.study.StudySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionInactiveScheduler {


    private final RedisTemplate<String, Object> redisTemplate;
    private final StudySessionService studySessionService;
    private static final String INACTIVE_INDEX_KEY = "index:study:sessions:inactive";
    private static final long INACTIVE_LIMIT_MINUTES = 10; // 10분 이상 비활성 시 삭제

    /**
     * 세션 스캔 스케줄러 설정
     */
    @Scheduled(fixedDelay=5*60*1000) //5분마다 실행
    private void flushInactiveSessions(){
        log.info("[SCHEDULER] flushExpiredSessions 실행됨");

        long nowMillis = System.currentTimeMillis();
        long cutoffMillis = nowMillis - (INACTIVE_LIMIT_MINUTES * 60 * 1000);

        // 1) inactiveSince가 cutoff 이전인 세션들 후보 조회
        Set<Object> candidates=redisTemplate.opsForZSet()
                .rangeByScore(INACTIVE_INDEX_KEY,0,cutoffMillis);

        if(candidates==null||candidates.isEmpty()) return;

        for(Object obj:candidates){
            String sessionKey=(String)obj;

            StudySession session=studySessionService.getStudySession(sessionKey);
            if(session==null){
                //value는 이미 TTL등으로 날아간 경우 -> 인덱스만 정리
                redisTemplate.opsForZSet().remove(INACTIVE_INDEX_KEY,sessionKey);
                continue;
            }

            //혹시 이미 ACTIVE로 바뀌었는데 인덱스가 안 지워져 있다는 방어 코드
            if(session.getStatus()!=Status.INACTIVE){
                redisTemplate.opsForZSet().remove(INACTIVE_INDEX_KEY,sessionKey);
                continue;
            }

            long minutesInactive=Duration.between(
                    session.getInactiveSince(),
                    java.time.LocalDateTime.now()
            ).toMinutes();

            if (minutesInactive < INACTIVE_LIMIT_MINUTES) {
                // 시간이 덜 지났는데 잘못 후보에 들어온 경우 방어 (cutoff 계산 오차 대비)
                continue;
            }

            //10분 이상 INACTIVE인 세션 -> 강제 종료/저장 대상
            session.setStatus(Status.EXIT);
            studySessionService.saveToDatabase(session);

            redisTemplate.delete(INACTIVE_INDEX_KEY);
            redisTemplate.opsForZSet().remove(INACTIVE_INDEX_KEY,sessionKey);

            log.info("[SCHEDULER] INACTIVE 만료 세션 저장 및 정리 완료: {}, inactive={}분",
                    sessionKey, minutesInactive);
        }
    }

}
