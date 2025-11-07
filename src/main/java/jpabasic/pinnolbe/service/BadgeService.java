package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import jpabasic.pinnolbe.dto.badge.LadyBugRequestDto;
import jpabasic.pinnolbe.repository.BadgeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public BadgeService(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }


    public void getSpeedHunterBadge(LadyBugRequestDto dto, String userId){
        String chapterId=dto.getChapterId();

        Badge badge=new Badge(userId,chapterId, BadgeType.SPEED_HUNTER);
        badgeRepository.save(badge);
    }
}
