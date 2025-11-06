package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.reward.Reward;
import jpabasic.pinnolbe.dto.reward.RewardRequestDto;
import jpabasic.pinnolbe.dto.reward.RewardResponseDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.service.login.RewardService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class RewardController {

    private final RewardService rewardService;
    private final UserService userService;

    public RewardController(RewardService rewardService,UserService userService) {
        this.rewardService = rewardService;
        this.userService = userService;
    }
    
    
    //reward 조회 //헤더에서 reward 조회할 수 있도록 수정할 필요 O
    @GetMapping("/myReward")
    public int myReward() {
        User user=userService.getUserInfo();
        return user.getReward();
    }

    @PostMapping("/upload-coin")
    @Operation(summary="얻거나 잃은 코인 내역 저장",
                description= """
                        - coin=얻거나 잃은 피넛 수
                        - category
                            GAME("게임 포인트"),
                            MISSION("방문 미션"),
                            REFUND("계좌 환급"),
                            PURCHASE("상품권 구매")
                        - description : 상세 설명
                        - isPositive: coin 획득 시 true, coin 잃었을 시 false
                        """)
    public ApiResponse<Reward> uploadCoin(@RequestBody RewardRequestDto dto) {
        User user=userService.getUserInfo();
        Reward result=rewardService.updateUserReward(dto,user);
        return ApiResponse.success("퀴즈에서 얻은 코인 저장을 완료했습니다.",result);
    }

    @GetMapping("/point-history")
    @Operation(summary="여태까지의 코인 내역 조회")
    public ApiResponse<Page<RewardResponseDto>> getRewardList(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="7") int size
    ){
        User user=userService.getUserInfo();
        Page<RewardResponseDto> result=rewardService.getUserRewardList(user.getId(),page,size);
        return ApiResponse.success("지금까지의 코인 내역입니다.",result);
    }
}
