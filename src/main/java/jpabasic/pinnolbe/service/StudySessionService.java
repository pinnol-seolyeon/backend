package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.StudySession;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.StudyLog;
import jpabasic.pinnolbe.domain.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class StudySessionService {

    @Autowired
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

        String key=SESSION_PREFIX+userId+":"+chapterId+":"+level;

        StudySession studySession = new StudySession(userId, level);
        try {
            //TTL 설정과 함께 Redis에 저장
            redisTemplate.opsForValue().set(key,studySession,SESSION_TTL, TimeUnit.SECONDS);
        }catch(DataAccessException e){
            throw new CustomException(ErrorCode.REDIS_SAVE_ERROR);
        }
    }

    /**
     * 학습 중 활동 중 Redis 세션 갱신 (INACTIVE)
     * @param user
     * @param summary
     */
    @Transactional
    public void sessionUpdate(User user, StudySessionSummaryDto summary){

        String key=SESSION_PREFIX+summary.getUserId()+":"+summary.getChapterId()+":"+summary.getLevel();
        StudySession session=getStudySession(key);

        if(session==null){
            //세션이 없으면 새로 생성
            startLevel(user,summary.getLevel(),summary.getChapterId());
            throw new CustomException(ErrorCode.SESSION_NOT_FOUND);
        }

        // 1. INACTIVE로 들어왔을 때
        if(session.getStatus()==Status.ACTIVE && summary.getStatus()== Status.INACTIVE){
            LocalDateTime lastActive=summary.getLastActive();
            session.setLastActive(lastActive);

            //총 학습 시간 누적 + 학습 시간대 세션에 저장
            updateTimeZone(session);
        }

        // 2. INACTIVE -> ACTIVE로 다시 바뀌는 시점
        if(session.getStatus()== Status.INACTIVE && summary.getStatus()==Status.ACTIVE){
            //idleDurationTime 계산
            long minutes=calculateIdleDuration(summary);

            session.addIdleDuration(minutes);
            session.setStatus(Status.ACTIVE);
            session.setLastActive(summary.getLastActive());
        }

        // 3. COMPLETE (해당 레벨 학습 완료)
        if(summary.getStatus()==Status.COMPLETED){
            saveToDatabase(session);
            redisTemplate.delete(key);
        }

        //세션 갱신
        redisTemplate.opsForValue().set(key,session,SESSION_TTL, TimeUnit.SECONDS);

    }

    /**
     * 총 학습 시간 누적 + 학습 시간대 세션에 저장
     * @param session
     */
    private void updateTimeZone(StudySession session){

        LocalDateTime now=LocalDateTime.now();

        LocalDateTime lastActive;
        //첫 inactive 인 경우
        if(session.getLastActive()==null){
            System.out.println("한 번도 inactive 된 적이 없습니당");
            lastActive=session.getStartTime(); //lastActiveTime=startTime
        }else{ //이미 여러 번 inactive <-> active 된 경우
            lastActive=session.getLastActive(); //lastActiveTime 값 그대로
        }

        //시간 차 계산
        long diffMinutes=Duration.between(lastActive,now).toMinutes();

        //총 학습 시간 누적
        session.addTotalDuration(diffMinutes);

        //시간대별 학습 시간 누적
        session.addDurationToTimeZone(lastActive,now);
    }

    /**
     * idleDuration 계산
     * @param summary
     */
    private long calculateIdleDuration(StudySessionSummaryDto summary) {
        LocalDateTime lastActiveTime=summary.getLastActive();
        LocalDateTime now=LocalDateTime.now();

        Duration duration=Duration.between(lastActiveTime,now);
        long minutes=duration.toMinutes();

        System.out.println("경과 시간:"+minutes+"분");
        return minutes;

    }

    @Transactional
    public void sessionComplete(User user,StudySessionSummaryDto summary){
        String key=SESSION_PREFIX+summary.getUserId()+":"+summary.getChapterId();
        flushExpiredSessions();
    }

    /**
     * 세션 스캔 스케줄러 설정
     */
    @Scheduled(fixedRate=5*60*1000) //5분마다 실행
    private void flushExpiredSessions(){
        Set<String> keys=redisTemplate.keys("study:session:*");
        if(keys==null) return;

        for(String key:keys){
            StudySession session=getStudySession(key);
            if(session==null) continue;

            //TTL 조회
            Long ttl=redisTemplate.getExpire(key, TimeUnit.SECONDS);

            //만료 임박 or 특정 조건 시 DB로 옮김
            if(ttl!=null && ttl <=60){
                saveToDatabase(session);
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * Redis에서 세션 조회
     * @param key
     * @return
     */
    private StudySession getStudySession(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * db에 session 저장
     * @param session
     */
    private void saveToDatabase(StudySession session){
        StudyLog log=new StudyLog();
    }



}
