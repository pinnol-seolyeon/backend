package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.Reward;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.reward.RewardDto;
import jpabasic.pinnolbe.repository.RewardRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    public RewardService(RewardRepository rewardRepository, UserRepository userRepository) {
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
    }

    public Reward myReward(String userId) {
        Reward reward = null;
        try {
            reward = rewardRepository.findByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reward;
    }

    public Map<String,Integer> uploadCoin(RewardDto dto, User user){
        Integer reward=user.getReward()+dto.getCoin();
        user.setReward(reward);
        userRepository.save(user);

        Map<String,Integer> map=new HashMap<>();
        map.put("coin",dto.getCoin());
        return map;
    }
}
