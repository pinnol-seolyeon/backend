package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.study.Chapter;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends MongoRepository<Chapter, ObjectId> {
    List<Chapter> findByBookId(String bookId, Sort sort);

    //ChapterIds 모두 가져오기
    List<Chapter> findByIdIn(List<ObjectId> ids);
    Optional<Chapter> findById(String id);

    Optional<Chapter> findByBookIdAndOrder(String bookId, int order);


}
