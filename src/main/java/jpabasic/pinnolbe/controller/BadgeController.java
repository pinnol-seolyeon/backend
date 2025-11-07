package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.badge.LadyBugRequestDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.BadgeService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/badge")
public class BadgeController {

    private final UserService userService;
    private final BadgeService badgeService;
    public BadgeController(UserService userService,BadgeService badgeService) {
        this.userService = userService;
        this.badgeService = badgeService;
    }

    @PostMapping("/catch-ladybug")
    @Operation(summary="스피드 사냥꾼 뱃지 획득",
            description="무당벌레 모두 2초 이내 클릭 성공 시 호출")
    public ApiResponse<Void> getSpeedHunterBadge(
            @RequestBody LadyBugRequestDto request
    ) {
        User user=userService.getUserInfo();
        badgeService.getSpeedHunterBadge(request,user.getId());
    }
}
