package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.study.Chapter;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends MongoRepository<Chapter, ObjectId> {
    //Slice는 다음 페이지가 있는지 여부만 체크
    Slice<Chapter> findByBookId(String bookId, Pageable pageable);
    Optional<Chapter> findById(String chapterId);
}
