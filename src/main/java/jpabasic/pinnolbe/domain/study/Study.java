package jpabasic.pinnolbe.domain.study;

import com.mongodb.lang.Nullable;
import jpabasic.pinnolbe.dto.study.CompletedChapter;
import jpabasic.pinnolbe.repository.study.BookRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

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

    @Nullable
    private List<CompletedChapter> completeChapter; //해당 교재에서 학습 완료된 단원id 리스트

    private Chapter chapter;


    public Study(String userId, String bookId,Chapter chapter) {
        this.userId = userId;
        this.bookId = bookId;
        this.chapter = chapter;
    }

}
