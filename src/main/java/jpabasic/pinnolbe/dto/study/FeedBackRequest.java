package jpabasic.pinnolbe.dto.study;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedBackRequest {
    private String chapterId;
    private int sentenceIndex;
    private String question;
    private String userAnswer;
}
