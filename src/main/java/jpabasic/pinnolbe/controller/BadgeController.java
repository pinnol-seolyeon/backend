package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.dto.badge.BadgeRequestDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.service.BadgeService;
import jpabasic.pinnolbe.service.login.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badge")
public class BadgeController {

    private final UserService userService;
    private final BadgeService badgeService;
    public BadgeController(UserService userService,BadgeService badgeService) {
        this.userService = userService;
        this.badgeService = badgeService;
    }

    @PostMapping("/win-badge")
    @Operation(summary="스피드 사냥꾼 뱃지 획득",
            description="""
            무당벌레 모두 2초 이내 클릭 성공 시 SPEED_HUNTER,
            무당벌레 연속 3마리 클릭 성공 시 FINE_HUNTER
            """)
    public ApiResponse<List<Badge>> getSpeedHunterBadge(
            @RequestBody BadgeRequestDto request
    ) {
        List<Badge> badges;
        User user=userService.getUserInfo();
        try{
            badges=badgeService.getBadge(request,user.getId());
        }catch(Exception e){
            throw new CustomException(ErrorCode.BADGE_SAVE_ERROR);
        }
        return ApiResponse.success("배지가 성공적으로 저장되었어요.",badges);
    }





}
