package jpabasic.pinnolbe.dto.study.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedBackRequest {
//    private String chapterId;
//    private int sentenceIndex;
    private String question;
    private String userAnswer;
    private String nextContext;
    private String chapter; //해당 단원 내용 전체
}
