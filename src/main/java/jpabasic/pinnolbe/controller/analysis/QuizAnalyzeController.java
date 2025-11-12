package jpabasic.pinnolbe.controller.analysis;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.analyze.quiz.QuizRecord;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.analyze.QuizRecordDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.service.BadgeService;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.analyze.RadarScoreService;
import jpabasic.pinnolbe.service.login.UserService;
import jpabasic.pinnolbe.service.study.StudySessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-result")
public class QuizAnalyzeController {

    private final QuizService quizService;
    private final BadgeService badgeService;

    public QuizAnalyzeController(QuizService quizService, BadgeService badgeService) {
        this.quizService = quizService;
        this.badgeService=badgeService;
    }

    // 이해도
    @PostMapping("")
    @Operation(summary="이번 주 이해도 저장·업데이트 및 틀린 문제 저장",
                description= """
                        퀴즈 완료 후 호출해주세요
                        """)
    public ApiResponse<List<QuizRecord>> saveResults(
            @RequestBody List<QuizAnalyzeDto> results) {
        if (results.isEmpty()) {
            return ApiResponse.fail("데이터 없음",400);
        }
        //이번 주 이해도 저장·업데이트
        quizService.upsertUnderstanding(results);
        //오답 저장
        List<QuizRecord> result=quizService.saveWrongQuizes(results);
        //퀴즈를 다 맞았을 경우 배지 획득
        badgeService.getSmartGamerBadge(results);

        return ApiResponse.success("이해도 및 집중도 저장 완료, 틀린 문제 기록이 저장되었습니다.",result);
    }

    @GetMapping
    @Operation(summary="챕터별로 틀린 문제들 조회")
    public ApiResponse<QuizRecordDto> getResults(
            @RequestParam String chapterId
    ){
        QuizRecordDto result=quizService.getWrongQuizes(chapterId);
        return ApiResponse.success("해당 챕터의 틀린 문제입니다.",result);
    }
}

