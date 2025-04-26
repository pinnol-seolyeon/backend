package jpabasic.pinnolbe.controller;

import jpabasic.pinnolbe.domain.Reward;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.service.login.RewardService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    
    //reward 조회
    @GetMapping("/myReward")
    public ResponseEntity<Reward> myReward() {
        User user=userService.getUserInfo();
        Reward myReward=rewardService.myReward(user.getId());
        return ResponseEntity.ok(myReward);
    }
}
