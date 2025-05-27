package jpabasic.pinnolbe.dto.study.feedback;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FeedBackResponse {

//    private String id;
    private List<String> feedbacks=new ArrayList<>(); //AI의 반응(리포트에는 필요X)
//    private String userId;
//    private String chapterId;
//    private int sentenceIndex;
    private List<String> questions=new ArrayList<>();
    private List<String> userAnswers=new ArrayList<>();

    public void add(String question,String userAnswer,String feedback){
        questions.add(question);
        userAnswers.add(userAnswer);
        feedbacks.add(feedback);

    }
}
