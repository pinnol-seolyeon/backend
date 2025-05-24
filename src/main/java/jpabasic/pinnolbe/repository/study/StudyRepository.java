package jpabasic.pinnolbe.repository.study;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import jpabasic.pinnolbe.domain.study.Study;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyRepository extends MongoRepository<Study, ObjectId> {

    Study findByUserId(String userId);
    Optional<Study> findByBookId(String bookId);

}
