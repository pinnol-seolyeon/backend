package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.ChapterProgress;
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

    //ChapterIds 모두 가져오기
    List<Chapter> findByIdIn(List<ObjectId> ids);
    Optional<Chapter> findById(String id);

    Optional<Chapter> findByBookIdAndOrder(String bookId, int order);
    // order 필드가 특정 값보다 작은 Chapter 전부 조회
    Slice<Chapter> findByBookIdAndOrderLessThan(String bookId,int order,Pageable pageable);

}
