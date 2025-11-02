package jpabasic.pinnolbe.controller.analysis;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.question.QueCollection;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.dto.analyze.RadarScoreDto;
import jpabasic.pinnolbe.dto.analyze.StudyTimeDetailDto;
import jpabasic.pinnolbe.dto.question.QueCollectionResponseDto;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.dto.study.StudyStatsDto;
import jpabasic.pinnolbe.dto.study.feedback.NowStudyingLevelDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.repository.question.QueCollectionRepository;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study-log")
@RequiredArgsConstructor
@Tag(name="학습 분석",description="학습 분석 관련 api")
public class StudyLogController {

    private final StudyLogService studyLogService;
    private final UserService userService;
    private final StudyService studyService;
    private final QueCollectionRepository queCollectionRepository;
    private final QuestionService questionService;
    private final RadarScoreService radarScoreService;

    @GetMapping("/this-week/chapters")
    @Operation(summary="이번주 학습 완료한 단원 개수")
    public ApiResponse<StudyStatsDto> getStudyStats() {
        User user = userService.getUserInfo();
        StudyStatsDto stats = studyService.getStudyStats(user.getId());
        return ApiResponse.success("이번 주 학습 완료한 단원 개수입니다.",stats);
    }

    @GetMapping("/now-studying")
    @Operation(summary="현재 단원 레벨")
    public ApiResponse<NowStudyingLevelDto> getNowStudyingLevel() {
        User user = userService.getUserInfo();
        String sessionLogId=user.getStudySessionLogId();
        NowStudyingLevelDto result=studyLogService.getNowStudyingLevel(user,sessionLogId);
        return ApiResponse.success("현재 학습중인 단원과 레벨입니다.",result);
    }

    @GetMapping("/overall-progress")
    @Operation(summary="전체 진행률")
    public ApiResponse<Double> getStudyProgress(){
        User user=userService.getUserInfo();
        Double progress=studyLogService.getStudyProgress(user);
        return ApiResponse.success("전체 진행률입니다.",progress);
    }

    @GetMapping("/weekly-pattern")
    @Operation(summary="주간 학습 패턴")
    public ApiResponse<List<StudyTimeDetailDto>> getTodayStudyTime() {
        User user = userService.getUserInfo();
        List<StudyTimeDetailDto> result = studyLogService.getTodayStudyTime(user);
        return ApiResponse.success("이번 주 주간 학습 패턴",result);
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
    @Operation(summary="학습 분석 화면에 표시할 질문 캘린더 날짜 추출")
    public List<LocalDate> getQuestionDates() {
        User user=userService.getUserInfo();
        String userId=user.getId();

        return queCollectionRepository.findByUserId(userId).stream()
                .map(q -> q.getDate().toLocalDate())
                .distinct()
                .collect(Collectors.toList());
    }


    @GetMapping("/questions/history")
    @Operation(summary="캘린더 해당 날짜 질문 내역 조회",
                description="date는 ISO type")
    public ApiResponse<List<QueCollectionResponseDto>> getDailyQnA(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<QueCollection> queCollection = queCollectionRepository.findAllByUserIdAndCreatedAtBetween(userId, start, end);
        List<QueCollectionResponseDto> dto = QueCollectionResponseDto.fromList(queCollection);
        return ApiResponse.success("조회 성공", dto);
    }


    @GetMapping("/radar-score")
    @Operation(summary="이번 주 engagement/focus/understanding/expression 학습분석 내용 제공")
    public ApiResponse<RadarScoreDto> getRadarScore() {
        return ApiResponse.success("이번 주 이해도 분석 내용입니다.",radarScoreService.getThisWeekRadarScore());
    }

    // 지난주 데이터까지
    @GetMapping("/radar-score/compare")
    @Operation(summary="이번 주 & 저번 주 engagement/focus/understanding/expression 학습분석 내용 제공")
    public ApiResponse<RadarScoreComparisonDto> getRadarComparison() {
        return ApiResponse.success("이번 주 이해도 분석 내용입니다.",radarScoreService.getThisAndLastWeekRadarScore());
    }

}

