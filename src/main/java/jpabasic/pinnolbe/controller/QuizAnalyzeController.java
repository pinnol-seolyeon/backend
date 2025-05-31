package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.analyze.RadarScoreComparisonDto;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
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
    private final UserService userService;

    public QuizAnalyzeController(WeeklyAnalysisRepository weeklyAnalysisRepository, RadarScoreService radarScoreService, UserService userService) {
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.userService = userService;
    }

    // 이해도, 집중도
    @PostMapping
    public ResponseEntity<String> saveResults(@RequestBody List<QuizAnalyzeDto> results) {
        if (results.isEmpty()) return ResponseEntity.badRequest().body("데이터 없음");

        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        // 정답 수 / 전체 퀴즈 수 계산
        int total = results.size();
        int correct = (int) results.stream().filter(QuizAnalyzeDto::isCorrect).count();

        // 평균 응답 시간 계산
        double avgResponseTime = results.stream()
                .mapToDouble(r -> r.getResponseTime() / 1000.0)
                .average()
                .orElse(0.0);

        // 새로운 document 생성 (매 학습마다 저장하는 구조)
        WeeklyAnalysis analysis = WeeklyAnalysis.builder()
                .userId(userId)
                .weekStartDate(weekStart)
                .understandingData(WeeklyAnalysis.UnderstandingData.builder()
                        .correct(correct)
                        .total(total)
                        .build())
                .focusData(WeeklyAnalysis.FocusData.builder()
                        .averageResponseTime(avgResponseTime)
                        .build())
                .analyzedAt(LocalDateTime.now())
                .build();

        weeklyAnalysisRepository.save(analysis);
        return ResponseEntity.ok("✅ 집중도 + 이해도 데이터 저장 완료");
    }
}

