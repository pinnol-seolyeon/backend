package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
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

    @GetMapping("/new-quiz")
    @Operation(summary="오답노트 기반 맞춤 퀴즈 생성")
    public String newQuiz(
            @RequestParam String chapterId
    ) {
        User user=userService.getUserInfo();
        String userId=user.getId();
        reviewService.restructureContent(userId,chapterId);
    }
}
