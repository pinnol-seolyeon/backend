package jpabasic.pinnolbe.dto.question;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class QuestionSessionDto {

    private List<String> questions=new ArrayList<>();
    private List<String> answers=new ArrayList<>();


    public void add(String question,String answer){
        questions.add(question);
        answers.add(answer);
    }

    public void clear(){
        questions.clear();
        answers.clear();
    }
}
