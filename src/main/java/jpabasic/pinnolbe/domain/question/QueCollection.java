package jpabasic.pinnolbe.domain.question;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QuestionAnswer;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection="queCollection")
//@NoArgsConstructor
//@RequiredArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor

public class QueCollection {

    @Id
    private String id;
    private String userId;
    private Date date; //질문한 날짜
    private int part; //단원

    private List<QuestionAnswer> questions;

    public QueCollection(int part, User user){
        date=new Date();
        this.part=part;
        this.userId = user.getId();
    }

//    public void addQuestionAnswer(String question){
//        if(questions==null){
//            questions=new ArrayList<QuestionAnswer>();
//        }
//        questions.add(new QuestionAnswer(question));
//    }
}
