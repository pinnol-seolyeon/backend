package jpabasic.pinnolbe.domain.study;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection="book")
@Getter @Setter
public class Book {

    @Id
    private String id;
    private int bookId;
    private String title;
//    private Long chapter_id;

    private List<Chapter> chapters;
}
