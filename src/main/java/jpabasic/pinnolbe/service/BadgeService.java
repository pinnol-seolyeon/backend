package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.dto.badge.LadyBugRequestDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.BadgeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public BadgeService(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Transactional
    public List<Badge> getHunterBadge(LadyBugRequestDto dto, String userId){
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

    public List<BadgeType> getBadgeList(User user, String chapterId){
        List<Badge> badges=badgeRepository.findByUserIdAndChapterId(user.getId(),chapterId);
        return badges.stream()
                .map(Badge::getBadgeType)
                .toList();
    }
}
