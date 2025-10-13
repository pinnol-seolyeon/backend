package jpabasic.pinnolbe.service.study;

import jpabasic.pinnolbe.domain.analyze.StudyLog;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.dto.analyze.AttendanceDto;
import jpabasic.pinnolbe.dto.analyze.StudySessionLogResponseDto;
import jpabasic.pinnolbe.dto.analyze.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.dto.study.CompletedChapter;
import jpabasic.pinnolbe.dto.study.FinishChaptersDto;
import jpabasic.pinnolbe.dto.study.StudyTimeStatsDto;
import jpabasic.pinnolbe.dto.study.feedback.NowStudyingLevelDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.analyze.StudyLogRepository;
import jpabasic.pinnolbe.repository.analyze.StudySessionLogRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.repository.study.BookRepository;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import jpabasic.pinnolbe.repository.study.StudyRepository;
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

    private final StudyLogRepository studyLogRepository;
    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final QueCollectionRepository queCollectionRepository;
    private final AskQuestionTemplate askQuestionTemplate;
    private final ChapterRepository chapterRepository;
    private final MongoTemplate mongoTemplate;
    private final BookRepository bookRepository;
    private final StudySessionLogRepository studySessionLogRepository;
    private final WeeklyAnalysisRepository weeklyAnalysisRepository;


    @Transactional
    //오늘 하루 공부 시간대 + 총 시간
    public TodayStudyTimeDto getTodayStudyTime(String userId) {
        LocalDate today = ZonedDateTime.now().toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<StudyLog> logs = studyLogRepository.findByUserIdAndStartTimeBetween(userId, startOfDay, endOfDay);

        long totalMinutes = logs.stream()
                .mapToLong(log -> Duration.between(log.getStartTime(), log.getEndTime()).toMinutes())
                .sum();

        int hours = (int) totalMinutes / 60;
        int minutes = (int) totalMinutes % 60;

        return new TodayStudyTimeDto(hours, minutes);
    }

    /**
     * 현재 레벨까지 학습한 시간대 & 시간 weekly_analysis에 저장
     */
    public void saveUntilStudyTime(StudySessionLogResponseDto dto) {

        WeeklyAnalysis weeklyAnalysis =
                weeklyAnalysisRepository.findByUserId(dto.getUserId())
                        .orElseGet(() -> new WeeklyAnalysis(dto.getUserId()));

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

        weeklyAnalysisRepository.save(weeklyAnalysis);
    }



    public AttendanceDto getAttendanceForMonth(String userId, YearMonth month) {
        List<StudyLog> logs = studyLogRepository.findByUserId(userId);

        Set<LocalDate> uniqueDates = logs.stream()
                .map(StudyLog::getDate)
                .filter(date -> YearMonth.from(date).equals(month))
                .collect(Collectors.toSet());

        return new AttendanceDto(new ArrayList<>(uniqueDates));
    }

    // 학습 선호 시간대 분석
    public StudyTimeStatsDto analyzeStudyTime(String studyId) {
        Study study = studyRepository.findById(new ObjectId(studyId))
                .orElseThrow(() -> new IllegalArgumentException("해당 study를 찾을 수 없습니다"));

        Set<CompletedChapter> chapters = study.getCompleteChapter();

        assert chapters != null;
        if (chapters.isEmpty()) {
            return new StudyTimeStatsDto("데이터 없음", Collections.emptyMap());
        }

        //시간 테스트
        System.out.println("✅ localDate: " + LocalDateTime.now());
        List<LocalDateTime> times=chapters.stream()
                .map(cc->cc.getCompletedAt())
                .collect(Collectors.toList());
        System.out.println("✅ DB localDate:"+times);


        // 최근 일주일 범위: 가장 최근 완료일 기준
        LocalDate latest = chapters.stream()
                .map(cc -> cc.getCompletedAt().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate startDate = latest.minusDays(6); // 최근 7일

        // 일~토 기준 요일 순서 지정
        List<DayOfWeek> weekOrder = List.of(
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        );

        Map<String, Map<String, Integer>> weeklyStats = new LinkedHashMap<>();
        for (DayOfWeek dow : weekOrder) {
            String label = dow.getDisplayName(TextStyle.SHORT, Locale.KOREAN); // 예: "일"
            weeklyStats.put(label, new HashMap<>());
        }

        Map<String, Integer> timeTypeCount = new HashMap<>();

        for (CompletedChapter cc : chapters) {
            //test

            LocalDateTime completed = cc.getCompletedAt();
            System.out.println("✅completed:"+completed);
            if (completed.toLocalDate().isBefore(startDate)) continue;

            int hour = completed.getHour();
            System.out.println("✅hout:"+hour);
            String type = hour >= 5 && hour < 12 ? "아침형"
                    : hour >= 12 && hour < 18 ? "낮형"
                    : hour >= 18 && hour <= 23 ? "밤형"
                    : "새벽형";

            timeTypeCount.merge(type, 1, Integer::sum);

            String dayLabel = completed.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            String periodKey = switch (type) {
                case "아침형" -> "morning";
                case "낮형" -> "afternoon";
                case "밤형" -> "evening";
                case "새벽형" -> "night";
                default -> "etc";
            };
            weeklyStats.getOrDefault(dayLabel, new HashMap<>())
                    .merge(periodKey, 1, Integer::sum);
        }

        timeTypeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
        String preferredType;

        // 고르게 분포되어 있으면 '언제든지좋아형'
        int total = timeTypeCount.values().stream().mapToInt(Integer::intValue).sum();
        int max = timeTypeCount.values().stream().max(Integer::compareTo).orElse(0);

        // max와의 차이가 1 이하인 시간대가 3개 이상이면 고르게 분포한 것으로 간주
        long evenlySpread = timeTypeCount.values().stream()
                .filter(c -> Math.abs(c - max) <= 1)
                .count();

        // 전체 학습 횟수가 3 이하이거나, 고르게 분포된 시간대가 3개 이상이면
        if (total <= 3 || evenlySpread >= 3) {
            preferredType = "언제든지 좋아형";
        } else {
            preferredType = timeTypeCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("분석불가");
        }

        return new StudyTimeStatsDto(preferredType, weeklyStats);
    }




    /// 이번 주 학습완료한 단원 개수 ///이번주 = (월요일 00:00~일요일 23:59)
    public FinishChaptersDto getCompletedWeek(String studyId){
        Study study=studyService.getStudyByString(studyId);
        Set<CompletedChapter> completedChapters=study.getCompleteChapter();
        
        if(completedChapters==null){
            return new FinishChaptersDto(0,0); //완료 단원이 아예 없을 경우
        }

        //이번주의 시작~끝
        LocalDate today=LocalDate.now();
        DayOfWeek dayOfWeek=today.getDayOfWeek(); //오늘이 무슨 요일인지
        LocalDate startOfWeek=today.minusDays(dayOfWeek.getValue()-1); //오늘로부터 요일 인덱스-1=이번주 월요일
        LocalDateTime start=startOfWeek.atStartOfDay();
        LocalDateTime end=start.plusDays(7); //다음 주 월요일 00:00


        //이번 주에 완료된 단원만 필터링
        long weekCount=completedChapters.stream()
                .filter(ch->{
                    LocalDateTime completed=ch.getCompletedAt();
                    return completed!=null && !completed.isBefore(start)&&completed.isBefore(end);
                })
                .count();

        long totalCount= completedChapters.size();
        return new FinishChaptersDto((int) weekCount,(int) totalCount);
    }

    //AI : 질문 요약
    public QuestionSummaryDto summaryQuestion(List<String> questions,User user){
        //AI에 유저의 질문리스트 전달
        try{
            return askQuestionTemplate.summaryQuestionsByAI(questions);
        }catch(RestClientException e){
            throw new RuntimeException("AI 서버 호출 중 오류 발생",e);
        }
    }


    //오늘 한 질문 개수들 보기
    public Integer countTodayQuestions(String userId) {
        //질문+답변 전체 불러오기
        List<String> questions=getTodayCollections(userId);

        return questions.size();
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

            LocalDateTime questionDate=collection.getDate();
            if (!questionDate.isBefore(startOfDay) && questionDate.isBefore(endOfDay)) {
                todayQuestions.add(collection);
                System.out.println("📅todayQuestions:"+todayQuestions);
            }
        }
        return todayQuestions;
    }

    //전체 진행률
    public double getStudyProgress(String userId){

        //전체 단원 개수
        int count=(int)mongoTemplate.count(new Query(), Chapter.class);

        //내가 학습 완료한 단원 개수
        int completedChapters=0;
        Study study=studyRepository.findByUserId(userId)
                .orElseThrow(()->new CustomException(ErrorCode.STUDY_NOT_FOUND));
        if (study.getCompleteChapter() != null) {
            completedChapters=study.getCompleteChapter().size();
        }

        //진행률 계산
        double progress=((double)completedChapters/count)*100;
        progress=Math.round(progress*10)/10.0;
        return progress;
    }

    //현재 학습 중인 교재 + 단원 제공
    public NowStudyingLevelDto getNowStudyingLevel(String userId){

        Study study=studyRepository.findByUserId(userId)
                .orElseThrow(()->new CustomException(ErrorCode.STUDY_NOT_FOUND));

        //현재 학습 중인 교재
        String bookId=study.getBookId();
        Book book = bookRepository.findById(new ObjectId(bookId))
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
        String title=book.getTitle();

        //현재 진도 단원 ID
        String currentId=study.getChapter()!=null?study.getChapter().getId().toString():null;
        List<String> chapters=book.getChapters();

        int index=-0;
        for(int i=0;i<chapters.size();i++){
            if(book.getChapters().get(i).equals(currentId)){
                index=i;
                break;
            }
        }

        NowStudyingLevelDto result=new NowStudyingLevelDto(title,index);
        return result;
    }




}
