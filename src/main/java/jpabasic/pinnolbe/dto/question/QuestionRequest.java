package jpabasic.pinnolbe.dto.question;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class QuestionRequest {

    int part;
    String question;
    Date now;

    public QuestionRequest(int part, String question) {
        this.part = part;
        this.question = question;
        now=new Date();
    }
}
