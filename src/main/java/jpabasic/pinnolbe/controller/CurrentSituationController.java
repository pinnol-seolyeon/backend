package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.currentSituation.CurrentSituationResDto;
import jpabasic.pinnolbe.dto.quiz.SolvedQuizResDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.CurrentSituationService;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/current-situation")
public class CurrentSituationController {

    private final CurrentSituationService currentSituationService;
    private final UserService userService;
    private final QuizService quizService;

    @GetMapping("")
    @Operation(summary="학습 현황 페이지")
    public ApiResponse<List<CurrentSituationResDto.CurrentChapterRes>> getSituationList() {
        User user=userService.getUserInfo();
        List<CurrentSituationResDto.CurrentChapterRes> result= currentSituationService.getCurrentSituation(user);
        return ApiResponse.success("학습 현황입니다.",result);
    }

    @GetMapping("/quizList")
    @Operation(summary="챕터별로 푼 문제들 조회")
    public ApiResponse<SolvedQuizResDto> getResults(
            @RequestParam String chapterId
    ){
        SolvedQuizResDto result=quizService.getSolvedQuizDetails(chapterId);
        return ApiResponse.success("해당 챕터에서 풀이한 문제들입니다.",result);
    }
}
