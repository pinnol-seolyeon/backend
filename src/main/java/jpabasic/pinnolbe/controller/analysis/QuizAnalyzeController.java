package jpabasic.pinnolbe.controller.analysis;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-result")
public class QuizAnalyzeController {

    private final QuizService quizService;

    public QuizAnalyzeController(QuizService quizService) {
        this.quizService = quizService;
    }

    // 이해도
    @PostMapping("")
    @Operation(summary="이번 주 이해도 저장·업데이트",
                description= """
                        퀴즈 완료 후 호출해주세요
                        """)
    public ApiResponse<String> saveResults(
            @RequestBody List<QuizAnalyzeDto> results) {
        if (results.isEmpty()) {
            return ApiResponse.fail("데이터 없음",400);
        }
        quizService.upsertUnderstanding(results);
        return ApiResponse.success("✅ 이해도·집중도 주차별 저장(또는 업데이트) 완료",null);
    }
}

