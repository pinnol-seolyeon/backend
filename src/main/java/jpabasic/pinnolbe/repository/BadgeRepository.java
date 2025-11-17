package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.badge.Badge;
import jpabasic.pinnolbe.domain.badge.BadgeType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends MongoRepository<Badge, String> {

    List<Badge> findByUserIdAndChapterId(String userId, String chapterId);
    boolean existsByUserIdAndChapteridAndBadgeType(
            String userId,
            String chapterId,
            BadgeType badgeType
    );
}
