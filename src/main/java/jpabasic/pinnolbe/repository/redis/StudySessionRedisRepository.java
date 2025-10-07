package jpabasic.pinnolbe.repository.redis;


import jpabasic.pinnolbe.domain.StudySession;
import org.springframework.data.repository.CrudRepository;

public interface StudySessionRedisRepository extends CrudRepository<StudySession, String> {
}
