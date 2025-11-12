package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.dto.analyze.QuizAnalyzeDto;
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
                .map(type -> Badge.builder()
                        .chapterId(dto.getChapterId())
                        .badgeType(type)
                        .userId(userId)
                        .build())
                .toList();
        badgeRepository.saveAll(badges);
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
        boolean hasAllCorrect=request.stream().allMatch(QuizAnalyzeDto::getIsCorrect);
        if(!hasAllCorrect) return;

        User user = userService.getUserInfo();
        StudySession session = studySessionService.getSessionByUser(user);
        String chapterId = session.getChapterId();

        BadgeRequestDto dto = new BadgeRequestDto(chapterId, BadgeType.SMART_GAMER);
        getBadge(dto, user.getId());

    }
}
