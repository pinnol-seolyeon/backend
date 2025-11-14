package jpabasic.pinnolbe.dto.quiz;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizNotes;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SolvedQuizResDto {
    private List<QuizNotes.QuizRecord> quizRecords;
    private double correctRate;

}
