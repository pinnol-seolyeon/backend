package jpabasic.pinnolbe.domain.question;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.dto.question.QuestionSessionDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
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
    private LocalDate date; //질문한 날짜
    private String chapterId; //단원

    private List<String> questions;
    private List<String> answers;

//    public void addQuestionAnswer(String question){
//        if(questions==null){
//            questions=new ArrayList<QuestionAnswer>();
//        }
//        questions.add(new QuestionAnswer(question));
//    }
}
