package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.study.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {


}
