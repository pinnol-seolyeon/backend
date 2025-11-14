package jpabasic.pinnolbe.dto.analyze;

import jpabasic.pinnolbe.domain.analyze.quiz.QuizRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuizRecordDto {
    private String chapterId;
    public List<EachQuiz> eachQuiz;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EachQuiz{
        private String question;
        private String correctAnswer;
        private String userAnswer;
        private Boolean isCorrect;

        public EachQuiz(QuizRecord record,String question,String correctAnswer){
            this.question = question;
            this.correctAnswer = correctAnswer;
            this.userAnswer = record.getUserAnswer();
            this.isCorrect = record.getIsCorrect();
        }


    }

}
