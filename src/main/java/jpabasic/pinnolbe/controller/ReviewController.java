package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.review.ReviewQuizResDto;
import jpabasic.pinnolbe.dto.review.TextReviewResDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.ReviewService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final UserService userService;
    private final ReviewService reviewService;

    public ReviewController(UserService userService,ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @GetMapping("/ai/quiz-review")
    @Operation(summary="[AI] 오답노트 기반 맞춤 퀴즈 생성")
    public ApiResponse<ReviewQuizResDto> newQuiz(
            @RequestParam String chapterId
    ) {
        User user=userService.getUserInfo();
        String userId=user.getId();
        ReviewQuizResDto result=reviewService.restructureContent(userId,chapterId);
        return ApiResponse.success("새로 생성된 퀴즈입니다.",result);
    }

    @GetMapping("/ai/text-review")
    @Operation(summary="[AI] 쌍둥이 문제 + 대화기록으로 1000자 정도의 교과서 제작")
    public ApiResponse<TextReviewResDto> newTextReview(
            @RequestParam String chapterId
    ){
        User user=userService.getUserInfo();
        String userId=user.getId();
        TextReviewResDto result=reviewService.createTextReview(userId,chapterId);
        return ApiResponse.success("새로운 복습 자료가 준비되었어요.",result);
    }

//    @GetMapping("")
//    @Operation(summary="복습해야할 단원들에 대한 리스트 제공")
//    public ApiResponse<ReviewQuizResDto> getReviewList(){
//        User user=userService.getUserInfo();
//        reviewService.getReviewList(user);
//    }


}
