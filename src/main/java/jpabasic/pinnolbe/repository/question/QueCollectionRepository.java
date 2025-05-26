package jpabasic.pinnolbe.repository.question;

import jpabasic.pinnolbe.domain.question.QueCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueCollectionRepository extends MongoRepository<QueCollection, String> {
        List<QueCollection> findByUserId(String userId);
}
