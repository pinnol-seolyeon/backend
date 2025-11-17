package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.dto.quiz.QuizAnalyzeDto;
import jpabasic.pinnolbe.dto.badge.BadgeRequestDto;
import jpabasic.pinnolbe.repository.BadgeRepository;
import jpabasic.pinnolbe.service.login.UserService;
import jpabasic.pinnolbe.service.study.StudySessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final StudySessionService studySessionService;
    private final UserService userService;

    public BadgeService(BadgeRepository badgeRepository, StudySessionService studySessionService, UserService userService) {
        this.badgeRepository = badgeRepository;
        this.studySessionService = studySessionService;
        this.userService = userService;
    }

    @Transactional
    public List<Badge> getBadge(BadgeRequestDto dto, String userId) {
        // 각 BadgeType별로 Badge 엔티티 생성
        List<Badge> badges = dto.getBadgeType().stream()
                //이미 존재하는 BadgeType은 제외
                .filter(type->!badgeRepository
                        .existsByUserIdAndChapteridAndBadgeType(userId,dto.getChapterId(),type))
                //남은 타입만 저장 객체 생성
                .map(type -> Badge.builder()
                        .chapterId(dto.getChapterId())
                        .badgeType(type)
                        .userId(userId)
                        .build())
                .toList();
        //저장할 게 있을 때만 saveAll
        if(!badges.isEmpty()) {
            badgeRepository.saveAll(badges);
        }
        return badges;
    }

    @Transactional
    public List<BadgeType> getBadgeList(User user, String chapterId) {
        List<Badge> badges = badgeRepository.findByUserIdAndChapterId(user.getId(), chapterId);
        return badges.stream()
                .map(Badge::getBadgeType)
                .toList();
    }

    @Transactional
    public void getSmartGamerBadge(List<QuizAnalyzeDto> request) {
        //스트림의 모든 요소에 대해 getIsCorrect()가 true를 반환해야
        boolean hasAllCorrect=request.stream().allMatch(QuizAnalyzeDto::getIsCorrect);
        if(!hasAllCorrect) return;

        //현재 진도 파악
        User user = userService.getUserInfo();
        StudySession session = studySessionService.getSessionByUser(user);
        String chapterId = session.getChapterId();

        BadgeRequestDto dto = new BadgeRequestDto(chapterId, BadgeType.SMART_GAMER);
        getBadge(dto, user.getId());

    }
}
