package jpabasic.pinnolbe.dto.question;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class QuestionAnswer {

    private List<String> question;
    private List<String> answer;


//    public QuestionAnswer(String question) {
//        this.question = question;
//    }
}
