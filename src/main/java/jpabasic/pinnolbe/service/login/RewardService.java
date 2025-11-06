package jpabasic.pinnolbe.service.login;

import jpabasic.pinnolbe.domain.reward.PointCategory;
import jpabasic.pinnolbe.domain.reward.Reward;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.dto.reward.RewardRequestDto;
import jpabasic.pinnolbe.dto.reward.RewardResponseDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.RewardRepository;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;

    public RewardService(
            RewardRepository rewardRepository,
            UserRepository userRepository,
            ChapterRepository chapterRepository) {
        this.rewardRepository = rewardRepository;
        this.userRepository = userRepository;
        this.chapterRepository = chapterRepository;
    }


    public Reward updateUserReward(RewardRequestDto dto, User user){
        //user 필드 업데이트
        Integer reward=user.getReward()+dto.getCoin();
        user.setReward(reward);
        userRepository.save(user);

        //chapterTitle 찾기
        Chapter chapter=chapterRepository.findById(dto.getChapterId())
                .orElseThrow(()->new CustomException(ErrorCode.CHAPTER_NOT_FOUND));

        //reward repository 업데이트
        Reward r=new Reward();
        r.setCategory(dto.getCategory());
        if(dto.getCategory()==PointCategory.GAME){
            r.setDescription(chapter.getChapterTitle()+"단원 퀴즈게임");
        }
        r.setUserId(user.getId());
        r.setPositive(dto.isPositive());
        r.setCoin(dto.getCoin());

        return rewardRepository.save(r);
    }

    //유저의 전체 피넛 획득 내역 조회
    public Page<RewardResponseDto> getUserRewardList(String userId, int page, int size){
        Pageable pageable= PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"));
        Page <Reward> rewards= rewardRepository.findAllByUserId(userId,pageable);
        return rewards.map(RewardResponseDto::from);
    }
}
