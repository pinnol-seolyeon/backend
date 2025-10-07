package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.StudySession;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.redis.StudySessionRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class StudySessionService {

    @Autowired
    private StudySessionRedisRepository repo;
    private final RedisTemplate<String,StudySession> redisTemplate;
    private static final String SESSION_PREFIX = "study:session:";
    private static final long SESSION_TTL = 60 * 60; // 1시간 TTL

    public StudySessionService(RedisTemplate<String, StudySession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 학습 시작 시 Redis에 세션 생성
     * @param user
     * @param level
     * @param chapterId
     */
    @Transactional
    public void startLevel(User user, int level, String chapterId) {
        String userId=user.getId();

        String key=SESSION_PREFIX+userId+":"+chapterId;

        StudySession studySession = new StudySession(userId, level);
        try {
            //TTL 설정과 함께 Redis에 저장
            redisTemplate.opsForValue().set(key,studySession,SESSION_TTL, TimeUnit.SECONDS);
        }catch(DataAccessException e){
            throw new CustomException(ErrorCode.REDIS_SAVE_ERROR);
        }
    }

    /**
     * 학습 중 활동 중 Redis 세션 갱신
     * @param user
     * @param summary
     */
    @Transactional
    public void sessionUpdate(User user,StudySession summary){

        String key=SESSION_PREFIX+summary.getUserId()+":"+summary.getChapterId();
        StudySession session=getStudySession(key);

        if(session==null){
            //세션이 없으면 새로 생성
            startLevel(user,summary.getLevel(),summary.getChapterId());
        }

        // 1. INACTIVE로 들어왔을 때
        if(session.getStatus()== Status.INACTIVE){
            session.setLastActive(summary.getLastActive());
        }

        // 2. INACTIVE -> ACTIVE로 다시 바뀌는 시점
        if(session.getStatus()== Status.INACTIVE && summary.getStatus()==Status.ACTIVE){
            //idleDurationTime 계산
            long minutes=calculateIdleDuration(summary);
            session.addIdleDuration(minutes);
        }

        //세션 갱신
        redisTemplate.opsForValue().set(key,session,SESSION_TTL, TimeUnit.SECONDS);

    }

    /**
     * idleDuration 계산
     * @param summary
     * @return
     */
    private long calculateIdleDuration(StudySession summary) {
        LocalDateTime lastActiveTime=summary.getLastActive();
        LocalDateTime now=LocalDateTime.now();

        Duration duration=Duration.between(lastActiveTime,now);
        long minutes=duration.toMinutes();

        System.out.println("경과 시간:"+minutes+"분");
        return minutes;

    }

    @Transactional
    public void sessionComplete(User user,StudySession summary){
        String key=SESSION_PREFIX+summary.getUserId()+":"+summary.getChapterId();

    }

    /**
     * Redis에서 세션 조회
     * @param key
     * @return
     */
    private StudySession getStudySession(String key){
        return redisTemplate.opsForValue().get(key);
    }



}
