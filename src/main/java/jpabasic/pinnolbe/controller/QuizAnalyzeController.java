package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-result")
public class QuizAnalyzeController {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final QuizService quizService;
    private final UserService userService;

    public QuizAnalyzeController(WeeklyAnalysisRepository weeklyAnalysisRepository, RadarScoreService radarScoreService, QuizService quizService, UserService userService) {
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.quizService = quizService;
        this.userService = userService;
    }

    // 이해도, 집중도
    @PostMapping("")
    @Operation(summary="이번 주 이해도+집중도 저장·업데이트")
    public ResponseEntity<String> saveResults(
            @RequestBody List<QuizAnalyzeDto> results) {
        if (results.isEmpty()) {
            return ResponseEntity.badRequest().body("데이터 없음");
        }
        quizService.upsertUnderstandingAndFocus(results);
        return ResponseEntity.ok("✅ 이해도·집중도 주차별 저장(또는 업데이트) 완료");
    }
}

