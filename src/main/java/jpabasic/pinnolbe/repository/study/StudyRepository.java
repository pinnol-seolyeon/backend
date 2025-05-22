package jpabasic.pinnolbe.repository.study;

import org.springframework.data.mongodb.repository.MongoRepository;
import jpabasic.pinnolbe.domain.study.Study;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyRepository extends MongoRepository<Study,String> {

    Study findByUserId(String userId);
    Optional<Study> findByBookId(String bookId);

}
