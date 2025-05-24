package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.study.Chapter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends MongoRepository<Chapter, ObjectId> {


}
