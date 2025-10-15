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


    private final RedisTemplate<String, StudySession> redisTemplate;
    private final StudySessionService studySessionService;

    private static final String SESSION_PREFIX = "study:session:";
    private static final long INACTIVE_LIMIT_MINUTES = 10; // 10분 이상 비활성 시 삭제

    /**
     * 세션 스캔 스케줄러 설정
     */
    @Scheduled(fixedDelay=5*60*1000) //5분마다 실행
    private void flushExpiredSessions(){
        log.info("[SCHEDULER] flushExpiredSessions 실행됨");

        Set<String> keys=redisTemplate.keys("study:session:*");
        if(keys==null) return;

        for(String key:keys) {
            StudySession session = studySessionService.getStudySession(key);
            if (session == null) continue;

            //상태가 INACTIVE 인 경우만 검사
            if (session.getStatus() == Status.INACTIVE && session.getInactiveSince() != null) {
                long minutesInactive = Duration.between(session.getInactiveSince(), LocalDateTime.now()).toMinutes();

                if (minutesInactive >= INACTIVE_LIMIT_MINUTES) {
                    //10분 이상 INACTIVE 상태 유지 -> 강제 종료 처리
                    session.setStatus(Status.EXITED);
                    log.info("[SessionCleanup] 세션 만료됨: " + key + " (" + minutesInactive + "분 동안 INACTIVE)");

                    studySessionService.saveToDatabase(session);
                    redisTemplate.delete(key);
                    log.info("[SCHEDULER] 세션 만료 저장 완료:{}", key);
                }
            }
        }
    }

}
