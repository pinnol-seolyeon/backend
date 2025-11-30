package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.quiz.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.quiz.QuizType;
import jpabasic.pinnolbe.dto.review.*;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.BadgeService;
import jpabasic.pinnolbe.service.ReviewService;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final QuizService quizService;
    private final BadgeService badgeService;

    public ReviewController(
            UserService userService,ReviewService reviewService,
            QuizService quizService,BadgeService badgeService) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.quizService = quizService;
        this.badgeService = badgeService;
    }

    @GetMapping("/ai/quiz-review")
    @Operation(summary="[AI] 복습하기 - 퀴즈풀기",
        description = "오답노트 기반 맞춤 퀴즈 생성")
    public ApiResponse<List<QuizReviewResponse>> newQuiz(
            @RequestParam int reviewCount,
            @RequestParam String chapterId
    ) {
        User user=userService.getUserInfo();
        String userId=user.getId();
        List<QuizReviewResponse> result=reviewService.createQuizReview(reviewCount,userId,chapterId);
        return ApiResponse.success("새로 생성된 퀴즈입니다.",result);
    }

    @GetMapping("/ai/text-review")
    @Operation(summary="[AI] 복습하기 시작",
            description = "쌍둥이 문제 + 대화기록으로 1000자 정도의 교과서 제작")
    public ApiResponse<TextReviewResponse> newTextReview(
            @RequestParam int reviewCount,
            @RequestParam String chapterId
    ){
        User user=userService.getUserInfo();
        String userId=user.getId();
        TextReviewResDto result=reviewService.createTextReview(reviewCount,userId,chapterId);
        TextReviewResponse response=new TextReviewResponse(chapterId,result.getTextbook());
        return ApiResponse.success("새로운 복습 자료가 준비되었어요.",response);
    }

    @GetMapping("")
    @Operation(summary="복습해야할 단원들에 대한 리스트 제공")
    public ApiResponse<Page<ReviewListResDto>> getReviewList(
            @RequestParam int page
    ){
        User user=userService.getUserInfo();
        Page<ReviewListResDto> result=reviewService.getReviewList(user,page);
        return ApiResponse.success("복습해야할 단원들입니다.",result);

    }

    @PostMapping("/review-completed")
    @Operation(summary="1차/2차 복습 완료 시 호출",description = "reviewCount=1차 학습 완료 시 -> 1, 2차 학습 완료 시 -> 2")
    public ApiResponse<Void> completeReview(
            @RequestParam int reviewCount,
            @RequestParam String chapterId,
            @RequestBody List<QuizAnalyzeDto> request
    ){
        User user=userService.getUserInfo();

        //복습 풀이 관련 로직
        //이번 주 이해도 저장·업데이트
        quizService.upsertUnderstanding(request);

        //오답 저장
        QuizType quizType;
        if(reviewCount==1){
            quizType=QuizType.FIRST_REVIEW;
        }else{
            quizType=QuizType.SECOND_REVIEW;
        }
        quizService.saveReviewQuizes(request,chapterId,quizType);
        //퀴즈를 다 맞았을 경우 배지 획득
        badgeService.getSmartGamerBadge(request,chapterId);

        //복습하기 끝내기
        reviewService.completeReview(user.getId(),reviewCount,chapterId);
        return ApiResponse.success(reviewCount+"차 복습을 완료했어요.",null);
    }



}
