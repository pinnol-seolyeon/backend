package jpabasic.pinnolbe.domain.study;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection="userFeedBack")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserFeedback {

    @Id
    private String id;
    private String feedback; //AI의 반응(리포트에는 필요X)
    private String userId;
    private String chapterId;
    private int sentenceIndex;
    private List<String> questions;
    private List<String> userAnswers;
    private LocalDateTime date; //질문한 날짜
}
