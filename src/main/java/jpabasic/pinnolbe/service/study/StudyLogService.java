package jpabasic.pinnolbe.service.study;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.analyze.StudyLog;
import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.dto.analyze.AttendanceDto;
import jpabasic.pinnolbe.dto.analyze.StudySessionLogResponseDto;
import jpabasic.pinnolbe.dto.analyze.StudyTimeDetailDto;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.dto.study.CompletedChapterDto;
import jpabasic.pinnolbe.dto.study.FinishChaptersDto;
import jpabasic.pinnolbe.dto.study.StudyStatsDto;
import jpabasic.pinnolbe.dto.study.StudyTimeStatsDto;
import jpabasic.pinnolbe.dto.study.feedback.NowStudyingLevelDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.StudySessionLogRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.repository.study.BookRepository;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import jpabasic.pinnolbe.domain.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.data.mongodb.core.query.Query;


import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final StudyService studyService;
    private final QueCollectionRepository queCollectionRepository;
    private final ChapterRepository chapterRepository;
    private final MongoTemplate mongoTemplate;
    private final BookRepository bookRepository;
    private final StudySessionLogRepository studySessionLogRepository;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;


    @Transactional
    //오늘 하루 공부 시간대 + 총 시간
    public List<StudyTimeDetailDto> getTodayStudyTime(User user) {
        LocalDate today = ZonedDateTime.now().toLocalDate();
//        LocalDateTime startOfDay = today.atStartOfDay();
//        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);
        WeeklyAnalysis analysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(user.getId(), weekStart)
                        .orElse(null);
        if (analysis==null||analysis.getWeeklyTimeZone()==null){
            return null;
        }

        List<StudyTimeDetailDto> result = new ArrayList<>();

        // weeklyTimeZone 안의 dayTimeZones 배열 순회
        for (WeeklyAnalysis.DayTimeZone dayZone : analysis.getWeeklyTimeZone().getDayTimeZones()) {
            DayOfWeek dayOfWeek = dayZone.getDayOfWeek(); // "WEDNESDAY"

            if (dayZone.getDayTimeZone() == null) continue;

            for (Map.Entry<String, Long> entry : dayZone.getDayTimeZone().entrySet()) {
                String timeZone = entry.getKey();  // "AFTERNOON"
                Long minutes = entry.getValue();   // 7

                result.add(new StudyTimeDetailDto(dayOfWeek, timeZone, minutes));
            }
        }

        return result;


    }


    /**
     * weeklyAnalysis에 focusing score 그때그때 저장
     */
    public void focusScoreUpdate(User user, double focusingScore){
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);

        WeeklyAnalysis analysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(user.getId(), weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(user.getId(), weekStart));

        //기존 점수 가져오기
        double oldScore=analysis.getFocusData().getFocusingScore();

        //새로운 점수 가져오기
        double newScore=oldScore+focusingScore;

        //새로운 점수를 객체에 반영
        analysis.getFocusData().setFocusingScore(newScore);

        //변경 내용 저장
        weeklyAnalysisRepository.save(analysis);
    }


    /**
     * 현재 레벨까지 학습한 시간대 & 시간 weekly_analysis에 저장
     */
    public WeeklyAnalysis saveUntilStudyTime(StudySessionLogResponseDto dto) {
        String userId=dto.getUserId();
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);

        WeeklyAnalysis weeklyAnalysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId,weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(userId,weekStart));

        LocalDate todayDate = LocalDate.now();
        System.out.println("📅 today = " + todayDate + " (" + todayDate.getDayOfWeek() + ")");

        // WeeklyTimeZone이 null이면 초기화
        if (weeklyAnalysis.getWeeklyTimeZone() == null) {
            weeklyAnalysis.setWeeklyTimeZone(new WeeklyAnalysis.WeeklyTimeZone(new ArrayList<>()));
        }

        // DayTimeZones 리스트가 null이면 초기화
        if (weeklyAnalysis.getWeeklyTimeZone().getDayTimeZones() == null) {
            weeklyAnalysis.getWeeklyTimeZone().setDayTimeZones(new ArrayList<>());
        }

        List<WeeklyAnalysis.DayTimeZone> dayTimeZones = weeklyAnalysis.getWeeklyTimeZone().getDayTimeZones();
        Optional<WeeklyAnalysis.DayTimeZone> existingDayTimeZoneOpt =
                dayTimeZones.stream()
                        .filter(dtz -> dtz.getDay().equals(todayDate)) // ✅ LocalDate 기준 비교
                        .findFirst();

        Map<String, Long> timeZoneDurations = dto.getTimeZoneDurations();
        System.out.println("✔️ timeZoneDurations:"+timeZoneDurations);

        if (existingDayTimeZoneOpt.isPresent()) {
            // ✅ 기존 DayTimeZone 업데이트
            WeeklyAnalysis.DayTimeZone todayTimeZone = existingDayTimeZoneOpt.get();
            Map<String, Long> existingTimeZone =
                    Optional.ofNullable(todayTimeZone.getDayTimeZone()).orElse(new HashMap<>());

            timeZoneDurations.forEach((key, newValue) ->
                    existingTimeZone.put(key, existingTimeZone.getOrDefault(key, 0L) + newValue)
            );

            todayTimeZone.setDayTimeZone(existingTimeZone);
        } else {
            // ✅ 없으면 새로 추가
            WeeklyAnalysis.DayTimeZone newTimeZone = WeeklyAnalysis.DayTimeZone.builder()
                    .day(todayDate)
                    .dayOfWeek(todayDate.getDayOfWeek())
                    .dayTimeZone(new HashMap<>(timeZoneDurations))
                    .build();

            dayTimeZones.add(newTimeZone);
        }
        System.out.println("weekly Analysis에 저장할 현재 레벨까지의 학습시간:"+weeklyAnalysis);
        WeeklyAnalysis result=weeklyAnalysisRepository.save(weeklyAnalysis);
        return result;
    }


    //오늘한 질문들만 보기
    public List<String> getTodayCollections(String userId){
        List<QueCollection> queCollections=getTodayQueCollection(userId);

        if(queCollections.isEmpty()){
            throw new IllegalStateException("아직 오늘 질문한 내용이 없습니다.");
        }

        List<String> result=new ArrayList<>();
        for(QueCollection queCollection:queCollections){
            if(queCollection.getQuestions()!=null){
                result.addAll(queCollection.getQuestions());
            }
            if(queCollection.getAnswers()!=null){
                result.addAll(queCollection.getAnswers());
            }
        }

        return result;
    }


    // 오늘의 queCollection
    public List<QueCollection> getTodayQueCollection(String userId) {
        System.out.println("😟"+userId);

        List<QueCollection> collections=queCollectionRepository.findByUserId(userId);
        System.out.println("✅✅"+collections.size());

        LocalDateTime startOfDay=LocalDate.now().atStartOfDay();
        System.out.println("📅오늘 날짜:"+startOfDay);

        LocalDateTime endOfDay=startOfDay.plusDays(1);
        System.out.println("📅마지노선:"+endOfDay);

        List<QueCollection> todayQuestions=new ArrayList<>();

        for(QueCollection collection:collections){
            LocalDateTime questionDate=collection.getCreatedAt();
            if (!questionDate.isBefore(startOfDay) && questionDate.isBefore(endOfDay)) {
                todayQuestions.add(collection);
                System.out.println("📅todayQuestions:"+todayQuestions);
            }
        }
        return todayQuestions;
    }

    //전체 진행률
    public double getStudyProgress(User user){

        //전체 단원 개수(모든 교재 포함)
        int count=(int)mongoTemplate.count(new Query(), Chapter.class);

        //내가 학습 완료한 단원 개수
        StudyStatsDto dto=studyService.getStudyStats(user.getId());
        int completedChapters=dto.getTotalCompleted();

        //진행률 계산
        double progress=((double)completedChapters/count)*100;
        progress=Math.round(progress*10)/10.0;
        return progress;
    }

    //현재 학습 중인 단원 + 레벨 제공
    public NowStudyingLevelDto getNowStudyingLevel(User user) {

        //현재 진행 중인 레벨이 user 필드에 studySessionLogId로 저장되어 있는 경우
        if (user.getStudySessionLogId() != null) {
            StudySessionLog log = studySessionLogRepository.findById(user.getStudySessionLogId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STUDY_SESSION_ID_NOT_FOUND));
            return toDto(log, 0);
        }

        List<StudySessionLog> list = studySessionLogRepository.findByUserId(user.getId());
        StudySessionLog latestLog = list.stream()
                .max(Comparator.comparing(StudySessionLog::getCreatedAt))
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_SESSION_LOG_NOT_FOUND));

        return latestLog.getStatus() == Status.COMPLETED
                ? toDto(latestLog, 1)
                : toDto(latestLog, 0);

    }

    private NowStudyingLevelDto toDto(StudySessionLog log, int levelOffset) {
        Chapter chapter=chapterRepository.findById(log.getChapterId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAPTER_NOT_FOUND));
        ObjectId objectId=new ObjectId(log.getBookId());
        Book book=bookRepository.findById(objectId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없어요."));
        String bookTitle=book.getTitle();
        int bookLevel=book.getBookLevel();
        int targetLevel=log.getLevel()+levelOffset;
        return new NowStudyingLevelDto(
                bookLevel,
                bookTitle,
                log.getChapterId(),
                chapter.getChapterTitle(),
                targetLevel
        );
    }

    public StudySessionLog findStudySessionLog(String sessionId){
        return studySessionLogRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDY_SESSION_ID_NOT_FOUND));
    }
}
