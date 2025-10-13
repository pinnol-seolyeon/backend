package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weekly-analysis")
@Tag(name="학습 분석")
public class WeeklyAnalysisController {

    private final UserService userService;

    public WeeklyAnalysisController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/progress")
    @Operation(summary="지난주 대비 진행률")
    public ApiResponse<?> getProgress() {
        User user=userService.getUserInfo();

        //지난 주 대비 진행률

        //학습 완료한 단원 수

        //현재 교재 레벨
    }
}
