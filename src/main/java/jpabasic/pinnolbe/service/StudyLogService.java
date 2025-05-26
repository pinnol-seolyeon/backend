package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.StudyLog;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.dto.AttendanceDto;
import jpabasic.pinnolbe.dto.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.dto.study.CompletedChapter;
import jpabasic.pinnolbe.dto.study.FinishChaptersDto;
import jpabasic.pinnolbe.repository.StudyLogRepository;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import jpabasic.pinnolbe.domain.User;
import org.springframework.web.client.RestClientException;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyLogService {

    private final StudyLogRepository studyLogRepository;
    private final StudyService studyService;
    private final QueCollectionRepository queCollectionRepository;
    private final AskQuestionTemplate askQuestionTemplate;
    private final MongoTemplate mongoTemplate;



    public TodayStudyTimeDto getTodayStudyTime(String userId) {
        LocalDate today = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate();
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

    public AttendanceDto getAttendanceForMonth(String userId, YearMonth month) {
        List<StudyLog> logs = studyLogRepository.findByUserId(userId);

        Set<LocalDate> uniqueDates = logs.stream()
                .map(StudyLog::getDate)
                .filter(date -> YearMonth.from(date).equals(month))
                .collect(Collectors.toSet());

        return new AttendanceDto(new ArrayList<>(uniqueDates));
    }

    public String getTodayStudyType(String userId) {
        LocalDate today = LocalDate.now();
        List<StudyLog> logs = studyLogRepository.findByUserIdAndDate(userId, today);

        if (logs.isEmpty()) return "랜덤형";

        Map<String, Integer> timeSlots = new HashMap<>();
        timeSlots.put("아침형", 0);
        timeSlots.put("낮형", 0);
        timeSlots.put("저녁형", 0);
        timeSlots.put("야행성", 0);

        for (StudyLog log : logs) {
            int hour = log.getStartTime().getHour();

            if (hour >= 5 && hour <= 9) timeSlots.computeIfPresent("아침형", (k, v) -> v + 1);
            else if (hour >= 10 && hour <= 16) timeSlots.computeIfPresent("낮형", (k, v) -> v + 1);
            else if (hour >= 17 && hour <= 21) timeSlots.computeIfPresent("저녁형", (k, v) -> v + 1);
            else timeSlots.computeIfPresent("야행성", (k, v) -> v + 1);
        }

        // 가장 많은 구간 찾기
        String maxType = Collections.max(timeSlots.entrySet(), Map.Entry.comparingByValue()).getKey();
        if (timeSlots.get(maxType) == 0) return "랜덤형";
        return maxType;
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




}
