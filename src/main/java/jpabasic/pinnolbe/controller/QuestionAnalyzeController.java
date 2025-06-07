package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.dto.question.QuestionSummaryDto;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class QuestionAnalyzeController {

    private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final UserService userService;

    public QuestionAnalyzeController(WeeklyAnalysisRepository weeklyAnalysisRepository, RadarScoreService radarScoreService, UserService userService) {
        this.weeklyAnalysisRepository = weeklyAnalysisRepository;
        this.userService = userService;
    }

    // 참여도
    @PostMapping("/save-question-summary")
    public ResponseEntity<String> saveQuestionSummary(@RequestBody QuestionSummaryDto dto) {
        if (dto == null || dto.getSize() == 0) {
            return ResponseEntity.badRequest().body("질문 없음");
        }

        User user = userService.getUserInfo();
        String userId = user.getId();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        WeeklyAnalysis analysis = WeeklyAnalysis.builder()
                .userId(userId)
                .weekStartDate(weekStart)
                .engagementData(WeeklyAnalysis.EngagementData.builder()
                        .questionCount(dto.getSize())
                        .build())
                .analyzedAt(LocalDateTime.now())
                .build();

        weeklyAnalysisRepository.save(analysis);

        return ResponseEntity.ok("✅ 질문 수 저장 완료");
    }
}
