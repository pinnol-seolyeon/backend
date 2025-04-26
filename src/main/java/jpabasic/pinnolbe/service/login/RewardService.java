package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.Reward;
import jpabasic.pinnolbe.repository.RewardRepository;
import org.springframework.stereotype.Service;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;

    public RewardService(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
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
}
