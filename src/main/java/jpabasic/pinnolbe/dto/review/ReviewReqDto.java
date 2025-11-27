package jpabasic.pinnolbe.dto.review;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewReqDto {
    private String user_id;
    private int level; //책
    private int order; //단원
    private List<QuizRecordForAI> quizRecords=new ArrayList<>();
}
