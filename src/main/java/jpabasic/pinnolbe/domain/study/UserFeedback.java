package jpabasic.pinnolbe.domain.study;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="userFeedBack")
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedback {

    @Id
    private String id;
    private String feedback; //AI의 반응(리포트에는 필요X)
    private String userId;
    private String chapterId;
    private int sentenceIndex;
    private String question;
    private String userAnswer;
}
