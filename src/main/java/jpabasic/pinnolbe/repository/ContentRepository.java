package jpabasic.pinnolbe.repository;

import jpabasic.pinnolbe.domain.study.Content;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContentRepository extends MongoRepository<Content, String> {
    // 만약 특정 강의(lectureId)에 해당하는 모든 문장을 시퀀스 기준으로 조회하고 싶다면:
    List<Content> findByLectureIdOrderBySequenceAsc(String lectureId);
}
