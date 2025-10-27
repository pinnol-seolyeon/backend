package jpabasic.pinnolbe.controller.analysis;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.analyze.AttendanceDto;
import jpabasic.pinnolbe.dto.analyze.TodayStudyTimeDto;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.dto.study.StudyStatsDto;
import jpabasic.pinnolbe.dto.study.StudyTimeStatsDto;
import jpabasic.pinnolbe.dto.study.feedback.NowStudyingLevelDto;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.service.question.QuestionService;
import jpabasic.pinnolbe.service.study.StudyLogService;
import jpabasic.pinnolbe.service.study.StudyService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
@Tag(name="학습 분석(수정 전)",description="학습 분석 관련 api")
public class StudyLogController {

    private final StudyLogService studyLogService;
    private final UserService userService;
    private final StudyService studyService;
    private final QueCollectionRepository queCollectionRepository;
    private final QuestionService questionService;

    @GetMapping("/stats")
    @Operation(summary="이번주 학습 완료한 단원 개수")
    public ResponseEntity<StudyStatsDto> getStudyStats() {
        User user = userService.getUserInfo();
        StudyStatsDto stats = studyService.getStudyStats(user.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/now-studying")
    @Operation(summary="현재 학습 단원(수정 전)")
    public ResponseEntity<NowStudyingLevelDto> getNowStudyingLevel() {
        User user = userService.getUserInfo();
        NowStudyingLevelDto result=studyLogService.getNowStudyingLevel(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/progress")
    @Operation(summary="전체 진행률(수정 전)")
    public ResponseEntity<Map<String, Double>> getStudyProgress(){
        User user=userService.getUserInfo();
        Double progress=studyLogService.getStudyProgress(user.getId());
        return ResponseEntity.ok(Map.of("전체 진행률",progress));
    }

    @GetMapping("/preferred-time")
    @Operation(summary = "선호 학습 시간대 및 요일별 학습 통계(수정 전)")
    public ResponseEntity<StudyTimeStatsDto> getStudyTimeStats() {
        User user = userService.getUserInfo();
        StudyTimeStatsDto stats = studyLogService.analyzeStudyTime(user.getStudyId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/today")
    @Operation(summary="오늘 학습 시간대 + 시간(수정 전)")
    public ResponseEntity<?> getTodayStudyTime() {
        User user = userService.getUserInfo();
        TodayStudyTimeDto result = studyLogService.getTodayStudyTime(user.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/calendar")
    @Operation(summary="오늘 학습한 시간(n시간 n분)(수정 전)")
    public ResponseEntity<AttendanceDto> getAttendance(
            @RequestParam int year,
            @RequestParam int month
    ) {
        User user = userService.getUserInfo();
        YearMonth yearMonth = YearMonth.of(year,


                month);
        AttendanceDto dto = studyLogService.getAttendanceForMonth(user.getId(), yearMonth);
        return ResponseEntity.ok(dto);
    }


    // 질문 내용 요약
    @PostMapping("/questions")
    @Operation(summary = "오늘 질문 내용 요약+오늘 질문 개수(수정 전)")
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



    @GetMapping("/questions/dates")
    @Operation(summary="학습 분석 화면에 표시할 질문 캘린더 날짜 추출(수정 전)")
    public List<LocalDate> getQuestionDates() {
        User user=userService.getUserInfo();
        String userId=user.getId();

        return queCollectionRepository.findByUserId(userId).stream()
                .map(q -> q.getDate().toLocalDate())
                .distinct()
                .collect(Collectors.toList());
    }


    @GetMapping("/questions/history")
    @Operation(summary="캘린더 해당 날짜 질문 내역(수정 전)")
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

