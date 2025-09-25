package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.analyze.AttendanceDto;
import jpabasic.pinnolbe.dto.analyze.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.dto.study.FinishChaptersDto;
import jpabasic.pinnolbe.dto.study.StudyStatsDto;
import jpabasic.pinnolbe.dto.study.StudyTimeStatsDto;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.service.QuestionService;
import jpabasic.pinnolbe.service.StudyLogService;
import jpabasic.pinnolbe.service.StudyService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
@Tag(name="학습 분석",description="학습 분석 관련 api")
public class StudyLogController {

    private final StudyLogService studyLogService;
    private final UserService userService;
    private final StudyService studyService;
    private final QueCollectionRepository queCollectionRepository;
    private final QuestionService questionService;

    @GetMapping("/stats")
    public ResponseEntity<StudyStatsDto> getStudyStats() {
        User user = userService.getUserInfo();

        StudyStatsDto stats = studyService.getStudyStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/preferred-time")
    @Operation(summary = "선호 학습 시간대 및 요일별 학습 통계")
    public ResponseEntity<StudyTimeStatsDto> getStudyTimeStats() {
        User user = userService.getUserInfo();
        StudyTimeStatsDto stats = studyLogService.analyzeStudyTime(user.getStudyId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayStudyTime() {
        User user = userService.getUserInfo();
        TodayStudyTimeDto result = studyLogService.getTodayStudyTime(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calendar")
    public ResponseEntity<AttendanceDto> getAttendance(
            @RequestParam int year,
            @RequestParam int month
    ) {
        User user = userService.getUserInfo();
        YearMonth yearMonth = YearMonth.of(year, month);
        AttendanceDto dto = studyLogService.getAttendanceForMonth(user.getId(), yearMonth);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/completed")
    @Operation(summary="학습완료한 단원 개수")
    public ResponseEntity<FinishChaptersDto> getCompletedWeek(){
        User user=userService.getUserInfo();
        String studyId=user.getStudyId();

        FinishChaptersDto dto=studyLogService.getCompletedWeek(studyId);
        return ResponseEntity.ok(dto);
    }

    // 질문 내용 요약
    @PostMapping("/questions")
    @Operation(summary = "오늘 질문 내용 요약+오늘 질문 개수")
    public ResponseEntity<?> summaryQuestions(){
        User user=userService.getUserInfo();
        String userId=user.getId();

        //오늘 한 질문들
        List<String> todayQAs=studyLogService.getTodayCollections(userId);
        if(todayQAs.isEmpty()){
            return ResponseEntity.ok("🥲 아직 오늘 질문한 내용이 없어요");
        }
        //질문 요약 api 호출
        QuestionSummaryDto result=studyLogService.summaryQuestion(todayQAs,user);

        return ResponseEntity.ok(result);
    }


    // 학습 분석화면에 표시할 질문 캘린더 날짜 추출
    @GetMapping("/questions/dates")
    public List<LocalDate> getQuestionDates() {
        User user=userService.getUserInfo();
        String userId=user.getId();

        return queCollectionRepository.findByUserId(userId).stream()
                .map(q -> q.getDate().toLocalDate())
                .distinct()
                .collect(Collectors.toList());
    }

    // 캘린더 해당 날짜 질문 내역
    @GetMapping("/questions/history")
    public List<QueCollection> getDailyQnA(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User user=userService.getUserInfo();
        String userId=user.getId();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return queCollectionRepository.findByUserIdAndDateBetween(userId, start, end);
    }




//    //질문 내용 그대로 전달
//    @GetMapping("/all-questions/today")
//    @Operation(summary="질문 내용 그대로 전달")
//    public ResponseEntity<?> getTodayQuestions(){
//        User user=userService.getUserInfo();
//        List<String> todayQAs=studyLogService.getTodayCollections(userId);
//    }




//    @GetMapping("/today/{userId}")
//    public TodayStudyTypeResponse getTodayStudyInfo(@PathVariable String userId) {
//        LocalDate today = LocalDate.now();
//        List<StudyLog> logs = studyLogService.getTodayStudyType(userId);
//
//        Duration total = logs.stream()
//                .map(log -> Duration.between(log.getStartTime(), log.getEndTime()))
//                .reduce(Duration.ZERO, Duration::plus);
//
//        int hours = (int) total.toHours();
//        int minutes = total.toMinutesPart();
//
//        String type = studyLogService.getTodayStudyType(userId);
//        return new TodayStudyTypeResponse(hours, minutes, type);
//    }


}

