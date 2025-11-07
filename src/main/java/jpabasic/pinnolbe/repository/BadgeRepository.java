package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.badge.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeRepository extends MongoRepository<Badge, String> {
}
