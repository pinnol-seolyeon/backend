package jpabasic.pinnolbe.scheduler;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.StudySession;
import jpabasic.pinnolbe.service.StudySessionService;
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

    private final RedisTemplate<String, StudySession> redisTemplate;
    private final StudySessionService studySessionService;

    @Scheduled(fixedDelay = 60 * 1000) // 1분마다
    private void flushExpiringSessions() {
        Set<String> keys = redisTemplate.keys("study:session:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl != null && ttl <= 60) {
                StudySession session = redisTemplate.opsForValue().get(key);
                if (session == null) continue;

                session.setStatus(Status.EXITED);
                studySessionService.saveToDatabase(session);
                redisTemplate.delete(key);
                log.info("[TTL] TTL 만료 세션 정리: {} (남은 TTL={}초)", key, ttl);
            }
        }
    }

}
