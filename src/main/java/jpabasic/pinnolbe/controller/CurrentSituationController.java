package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.currentSituation.CurrentSituationResDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.CurrentSituationService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/current-situation")
public class CurrentSituationController {

    private final CurrentSituationService currentSituationService;
    private final UserService userService;

    @GetMapping("/list")
    @Operation(summary="학습 현황 페이지")
    public ApiResponse<Slice<CurrentSituationResDto.CurrentChapterRes>> getSituationList(
            int page
    ) {
        User user=userService.getUserInfo();
        Slice<CurrentSituationResDto.CurrentChapterRes> result= currentSituationService.getCurrentSituation(user,page);
        return ApiResponse.success("학습 현황입니다.",result);
    }
}
