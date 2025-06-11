package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.Reward;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.reward.RewardDto;
import jpabasic.pinnolbe.service.login.RewardService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @Operation(summary="퀴즈에서 얻은 코인")
    public ResponseEntity<Map<String,Integer>> uploadCoin(@RequestBody RewardDto dto) {
        User user=userService.getUserInfo();
        Map<String,Integer> result=rewardService.uploadCoin(dto,user);
        return ResponseEntity.ok(result);
    }
}
