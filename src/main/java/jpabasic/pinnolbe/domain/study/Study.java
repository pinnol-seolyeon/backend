package jpabasic.pinnolbe.domain.study;

import com.mongodb.lang.Nullable;
import jpabasic.pinnolbe.repository.study.BookRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection="study")
@Getter
@Setter
@NoArgsConstructor
public class Study {

    private BookRepository bookRepository;
    public Study(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }



    @Id
    private String id;
    private String userId;
    private String bookId;

    private Chapter chapter;


    //첫 단원일 때
    public Study(String userId,String bookId){
        this.userId = userId;
        this.bookId = bookId;

        Book book = bookRepository.findById(bookId).orElseThrow(() ->
                new IllegalArgumentException("해당 ID의 책이 존재하지 않습니다: " + bookId)
        );

        List<Chapter> chapterList=book.getChapters();

        this.chapter=chapterList.get(0);
    }

}
