package jpabasic.pinnolbe.repository.question;

import jpabasic.pinnolbe.domain.question.QueCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueCollectionRepository extends MongoRepository<QueCollection, String> {

}
