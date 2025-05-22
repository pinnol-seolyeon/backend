package jpabasic.pinnolbe.domain.study;

import com.mongodb.lang.Nullable;
import jpabasic.pinnolbe.repository.study.BookRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection="study")
@Getter
@Setter
@NoArgsConstructor
public class Study {



    @Id
    @Field("_id")
    private ObjectId id;
    private String userId;
    private String bookId;

    private Chapter chapter;


    public Study(String userId, String bookId,Chapter chapter) {
        this.userId = userId;
        this.bookId = bookId;
        this.chapter = chapter;
    }

}
