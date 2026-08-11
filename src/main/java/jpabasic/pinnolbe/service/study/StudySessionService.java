package jpabasic.pinnolbe.service.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import jpabasic.pinnolbe.dto.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.dto.analyze.StudySessionLogResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.analyze.StudySessionLogRepository;
import jpabasic.pinnolbe.repository.study.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_PREFIX = "study:session:";
    private static final String INDEX_SESSION_PREFIX = "index:study:session:";
    private static final String INDEX_INACTIVE_SESSION_PREFIX = "index:study:sessions:inactive";
    private static final long SESSION_TTL = 60 * 60; // 1시간 TTL
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StudySessionLogRepository studySessionLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final StudyLogService studyLogService;

    /// 세션 조회 로직
    public StudySession getSessionByUser(User user) {
        String userId = user.getId();
        String indexKey=INDEX_SESSION_PREFIX+userId;

        //최신 1개만 불러오기
        Set<Object> result=redisTemplate.opsForZSet()
                .reverseRange(indexKey,0,0);

        if(result==null||result.isEmpty()) return null;

        String sessionKey=(String)result.iterator().next();
        return getStudySession(sessionKey);
    }

    /** 학습 시작 시 Redis에 세션 생성 */
    @Transactional
    public String startLevel(User user, int level, String chapterId,String bookId) {
        System.out.println("📘 [startLevel] 호출됨: userId=" + user.getId() + ", level=" + level + ", chapterId=" + chapterId);

        String userId = user.getId();
        String key = SESSION_PREFIX + userId + ":" + chapterId + ":" + level;
        String indexKey=INDEX_SESSION_PREFIX+userId;

        // Redis 세션 객체 생성
        StudySession studySession = new StudySession(key, userId, chapterId, bookId,level);
        System.out.println("✅ [startLevel] StudySession 객체 생성 완료");

        // StudySessionLog 확인 또는 생성
        String studySessionLogId = findStudySessionLog(userId, chapterId, bookId, level);

        user.setStudySessionLogId(studySessionLogId);
        userRepository.save(user);
        System.out.println("✅ [startLevel] User 문서 업데이트 완료, studySessionLogId=" + studySessionLogId);

        try {
            //세션 데이터 저장
            redisTemplate.opsForValue().set(key, studySession, SESSION_TTL, TimeUnit.SECONDS);

            //인덱스에 세션 키 등록(ZSET)
            long score=getVsetScore();
            redisTemplate.opsForZSet().add(indexKey, key, score);
            System.out.println("✅ [startLevel] Redis 세션 저장 성공 key=" + key);
        } catch (DataAccessException e) {
            System.out.println("❌ [startLevel] Redis 저장 실패: " + e.getMessage());
            throw new CustomException(ErrorCode.REDIS_SAVE_ERROR);
        }

        return studySessionLogId;
    }

    /** StudySessionLog 찾기/생성 */
    private String findStudySessionLog(String userId, String chapterId, String bookId,int level) {
        System.out.println("🔍 [findStudySessionLog] 실행 중...");
        Optional<StudySessionLog> existingLogOpt =
                studySessionLogRepository.findByUserIdAndChapterIdAndLevel(userId, chapterId, level);
        System.out.println("existingLogOpt="+existingLogOpt.orElse(null));
        if (existingLogOpt.isPresent()) {
            System.out.println("✅ 기존 StudySessionLog 존재, ID=" + existingLogOpt.get().getId());
            StudySessionLog existing=existingLogOpt.get();
            //기존에 db에 저장되어 있던 StudySessionLog를 ACTIVE 상태로 변환
            existing.setStatus(Status.ACTIVE);
            studySessionLogRepository.save(existing);
            return existing.getId();
        } else {
            StudySessionLog log = new StudySessionLog(userId, chapterId, bookId,level);
            log.setStatus(Status.ACTIVE);
            String id = studySessionLogRepository.save(log).getId();
            System.out.println("🆕 새로운 StudySessionLog 생성됨, ID=" + id);
            return id;
        }
    }

    /**
     * 학습 중 세션 갱신
     *
     * @return
     */
    @Transactional
    public StudySessionLogResponseDto sessionUpdate(User user, StudySessionSummaryDto summary) {
        System.out.println("🌀 [sessionUpdate] 호출됨, userId=" + user.getId() + ", status=" + summary.getStatus());
        int level=summary.getLevel();
        String userId=user.getId();
        String key = SESSION_PREFIX + userId+":"+summary.getChapterId()+":"+level;
        String indexKey=INDEX_SESSION_PREFIX+userId;
        String inactiveIndexKey=INDEX_INACTIVE_SESSION_PREFIX;
        StudySession session = getStudySession(key);

        if (session == null) {
            System.out.println("⚠️ Redis 세션이 존재하지 않아 새로 생성함");
            startLevel(user, summary.getLevel(), summary.getChapterId(),summary.getBookId());
            session = getStudySession(key);
        }

        LocalDateTime lastActive=summary.getLastActive();

        // ACTIVE → INACTIVE
        if (session.getStatus() == Status.ACTIVE && summary.getStatus() == Status.INACTIVE) {
            System.out.println("🔻 [ACTIVE → INACTIVE] 전환 감지");

            System.out.println("🕒 inactiveSince=" + session.getInactiveSince() + ", lastActive=" + session.getLastActive());
            //학습한 시간 + 시간대 설정
            updateTimeZone(session);
            session.setStatus(Status.INACTIVE);
            session.setInactiveSince(lastActive);
            session.setLastActive(lastActive);

            //INACTIVE가 되는 순간 집중도 점수 감점
            double newScore=summary.getMinusFocusingScore()+session.getFocusingScore(); //나중에 5점에서 감점될 점수
            session.setFocusingScore(newScore);
            //weeklyAnalysis에 업데이트
            studyLogService.focusScoreUpdate(user,newScore);

            //세션 자체 저장
            redisTemplate.opsForValue().set(key, session, SESSION_TTL, TimeUnit.SECONDS);

            // INACTIVE ZSET등록 (lastActive 기준)
            long inactiveScore=getInactiveScore(lastActive);
            redisTemplate.opsForZSet().add(inactiveIndexKey, key, inactiveScore);

            // TTL ZSET도 갱신 (서버 시간)
            redisTemplate.opsForZSet().add(indexKey, key, getVsetScore());
        }

        // INACTIVE → ACTIVE
        if (session.getStatus() == Status.INACTIVE && summary.getStatus() == Status.ACTIVE) {
            System.out.println("🔺 [INACTIVE → ACTIVE] 전환 감지");
            long minutes = calculateIdleDuration(summary);
            session.addIdleDuration(minutes);
            session.setStatus(Status.ACTIVE);
            session.setLastActive(lastActive);
            System.out.println("⏱️ idleDuration 추가: " + minutes + "분");

            //세션 자체 저장
            redisTemplate.opsForValue().set(key, session, SESSION_TTL, TimeUnit.SECONDS);

            // INACTIVE ZSET에서 제거
            redisTemplate.opsForZSet().remove(inactiveIndexKey,key);

            // TTL ZSET score → 서버 시간으로 갱신
            redisTemplate.opsForZSet().add(indexKey, key, getVsetScore());
        }

        // COMPLETE
        if (summary.getStatus() == Status.COMPLETED) {
            System.out.println("🏁 [COMPLETE] 감지 - DB 저장 로직 실행");
            session.setStatus(Status.COMPLETED);
            //학습한 시간 + 시간대 설정
            updateTimeZone(session);

            StudySessionLogResponseDto dto=saveToDatabase(session); //studySessionLog에 저장
            System.out.println("✔️ dto: "+ dto);

            if(session.getLevel()<=5){
                //다음 레벨에 대한 StudySessionLog 저장해야
                StudySessionLog newSession=new StudySessionLog(session.getUserId(), session.getChapterId(), session.getBookId(),session.getLevel()+1);
                StudySessionLog saved=studySessionLogRepository.save(newSession);
                user.setStudySessionLogId(saved.getId());
                userRepository.save(user);
            }else{
                //레벨 6일 경우, sessionLog null 처리 후, finishChapter에서 다음 chapter로 넘어가도록 설정
                user.setStudySessionLogId(null);
                userRepository.save(user);
            }

            //redis 세션 삭제
            boolean deleted=redisTemplate.delete(key);
            System.out.println("🧹 Redis 세션 삭제 완료"+deleted);

            //레벨 학습완료 후, 해당 레벨 학습 시간 weeklyAnalysis에 저장
            WeeklyAnalysis weeklyAnalysis=studyLogService.saveUntilStudyTime(dto);
            String weeklyId= weeklyAnalysis.getId();
            dto.setWeeklyAnalysisId(weeklyId);

            //두 ZSET에서 모두 삭제
            redisTemplate.opsForZSet().remove(indexKey,key);
            redisTemplate.opsForZSet().remove(inactiveIndexKey,key);

            return dto;
        }

        //EXIT (유저가 학습하기 창에서 나감)
        if(summary.getStatus()==Status.EXIT){
            System.out.println("🚫 [EXIT] 유저가 학습하기 창에서 나감 ");
            session.setStatus(Status.EXIT);
            StudySessionLogResponseDto dto=saveToDatabase(session); //studySessionLog에 저장

            //redis 삭제
            boolean deleted=redisTemplate.delete(key);

            //user 필드에 현재 세션 진도 저장
            user.setStudySessionLogId(dto.getId());
            userRepository.save(user);

            //두 ZSET에서 모두 삭제
            redisTemplate.opsForZSet().remove(indexKey,key);
            redisTemplate.opsForZSet().remove(inactiveIndexKey,key);

            return dto;
        }

//        //ZSET score(=timestamp) 갱신
//        redisTemplate.opsForZSet().add(indexKey,key,System.currentTimeMillis());
//        redisTemplate.expire(indexKey,SESSION_TTL, TimeUnit.SECONDS); //⭐❓

        System.out.println("💾 Redis 세션 갱신 완료 key=" + key);

        return null;
    }


    /** 총 학습 시간 및 시간대 누적 */
    private void updateTimeZone(StudySession session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActive = session.getLastActive() != null
                ? session.getLastActive()
                : session.getStartTime();

        long diffMinutes = Duration.between(lastActive, now).toMinutes();
        System.out.println("✔️ 총 학습 시간 :"+diffMinutes);
        
        session.addTotalDuration(diffMinutes);
        session.addDurationToTimeZone(lastActive, now);
        System.out.println("✔️ 총 학습 시간 및 시간대 누적 완료");
    }

    /** idleDuration 계산 */
    private long calculateIdleDuration(StudySessionSummaryDto summary) {
        LocalDateTime lastActive = summary.getLastActive();
        LocalDateTime now = LocalDateTime.now();

        long minutes = Duration.between(lastActive, now).toMinutes();
        System.out.println("⏳ 경과 시간: " + minutes + "분");
        return minutes;
    }

    /** Redis에서 세션 조회 */
    public StudySession getStudySession(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            System.out.println("⚠️ [getStudySession] key=" + key + " 값이 null");
            return null;
        }

        StudySession session = (value instanceof StudySession s)
                ? s
                : objectMapper.convertValue(value, StudySession.class);

        if (session.getTimeZoneDurations() == null)
            session.setTimeZoneDurations(new HashMap<>());

        System.out.println("✅ [getStudySession] key=" + key + " 세션 조회 성공");
        return session;
    }

    /** DB에 세션 저장 */
    public StudySessionLogResponseDto saveToDatabase(StudySession session) {
        System.out.println("💾 [saveToDatabase] 실행 시작, userId=" + session.getUserId());
        String userId = session.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String currentLogId = user.getStudySessionLogId();
        StudySessionLog existingLog = studySessionLogRepository.findById(currentLogId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_SESSION_LOG_NOT_FOUND));



        //기존 sessionLog에 저장되어 있던 학습시간+redis에 신규로 저장되어 있던 학습시간
        long newDuration = existingLog.getTotalDuration() + session.getTotalDuration();
        existingLog.setTotalDuration(newDuration);
        //기존 sessionLog에 저장되어 있던 timeZone 별 학습시간+redis에 신규로 저장되어 있던 timeZone별 학습시간
        mergeTimeZoneDuration(existingLog, session);
        existingLog.setStatus(session.getStatus());
        StudySessionLog result=studySessionLogRepository.save(existingLog);

        System.out.println("✅ [saveToDatabase] 저장 완료, totalDuration=" + newDuration);
        return StudySessionLogResponseDto.toStudySessionLog(result);
    }

    /** timeZoneDuration 병합 */
    private void mergeTimeZoneDuration(StudySessionLog existingLog, StudySession session) {
        Map<String, Long> newDurations = session.getTimeZoneDurations();
        if (newDurations == null || newDurations.isEmpty()) {
            System.out.println("⚠️ [mergeTimeZoneDuration] 새로운 데이터 없음");
            return;
        }

        Map<String, Long> existingDurations =
                Optional.ofNullable(existingLog.getTimeZoneDurations())
                        .orElseGet(HashMap::new);

        newDurations.forEach((zone, value) ->
                existingDurations.merge(zone, value, Long::sum));

        existingLog.setTimeZoneDurations(existingDurations);
        System.out.println("✅ [mergeTimeZoneDuration] 병합 완료");
    }

    /*TTL VSET - 서버 시간으로 VSET score 구하기**/
    private long getVsetScore(){
        return System.currentTimeMillis();
    }

    /*inactive VSET - lastActive 기반으로 VSET score 구하기**/
    private long getInactiveScore(LocalDateTime lastActive){
        return lastActive.atZone(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();
    }


}
