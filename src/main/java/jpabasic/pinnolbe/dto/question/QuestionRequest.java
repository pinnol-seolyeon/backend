package jpabasic.pinnolbe.dto.question;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class QuestionRequest {

//    int part; //chapter별?
    String question;

    public QuestionRequest(String question) {
//        this.part = part;
        this.question = question;
    }
}
