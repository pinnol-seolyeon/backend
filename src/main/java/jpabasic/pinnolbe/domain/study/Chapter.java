package jpabasic.pinnolbe.domain.study;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection="chapter")
@Getter
@Setter
public class Chapter {

    @Id
    @Field("_id")
    private ObjectId id;
    private String chapterTitle;
    private String content;
    private String objective;
    private String imgUrl;
}
