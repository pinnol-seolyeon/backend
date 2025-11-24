package jpabasic.pinnolbe.scheduler;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.service.study.StudySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionTTLScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StudySessionService studySessionService;
    private static final long SESSION_TTL = 60 * 60; // 1시간 TTL
    private static final long SAVE_MARGIN=60; //1분 

    long now=System.currentTimeMillis();
    long expiredCutoff=now-SESSION_TTL*1000;
    long expireSoonCutoff=now-(SESSION_TTL*1000)+(SAVE_MARGIN*1000); //TTL 1분 남은 세션들 찾기 위한 cutoff

    /// 이미 TTL로 삭제된 dead-key 정리
    @Scheduled(fixedDelay = 60 * 1000) // 1분마다
    private void flushExpiredSessions() {
        //오래된 세션만 가져오기
        Set<Object> expired=redisTemplate.opsForZSet()
                .rangeByScore("index:study:sessions",0,expiredCutoff);

        for(Object k:expired){
            String sessionKey=(String) k;

            //Value는 이미 TTL로 사라졌을 확률이 높음
            StudySession session= (StudySession) redisTemplate.opsForValue().get(sessionKey);

            //Session==null -> TTL 삭제된 dead key
            //session!=null -> 의도치 않은 지연 -> DB저장
            if(session!=null){
                session.setStatus(Status.EXIT);
                studySessionService.saveToDatabase(session);
                redisTemplate.delete(sessionKey);
            }

            //인덱스 제거
            redisTemplate.opsForZSet().remove("index:study:sessions",sessionKey);
        }

    }

    /// TTL 직전이어서 DB에 저장해야 하는 세션들
    @Scheduled(fixedDelay = 60 * 1000) // 1분마다
    private void flushSoonExpiringSessions() {
        Set<Object> expiringSoon=redisTemplate.opsForZSet()
                .rangeByScore("index:study:sessions",0,expireSoonCutoff);
        for(Object k:expiringSoon){
            String sessionKey=(String) k;
            StudySession session= (StudySession) redisTemplate.opsForValue().get(sessionKey);
            if(session!=null){
                session.setStatus(Status.EXIT);
                studySessionService.saveToDatabase(session);
                redisTemplate.delete(sessionKey);
                redisTemplate.opsForZSet().remove("index:study:sessions",sessionKey);
            }
        }
    }

}
